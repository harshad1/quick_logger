package dev.quicklogger

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class LogUndoSnapshot(
    val fileUri: String,
    val fileExisted: Boolean,
    val previousContent: String,
)

class LogFileService(private val context: Context) {
    private val headingRegex = Regex("^(#{1,6})\\s+(.+?)\\s*#*\\s*$")

    fun readTemplateHeadings(templateUri: Uri?): List<HeadingSuggestion> {
        if (templateUri == null) return emptyList()
        return runCatching {
            readText(templateUri).lineSequence().mapNotNull { line ->
                val match = headingRegex.matchEntire(line.trim()) ?: return@mapNotNull null
                HeadingSuggestion(match.groupValues[1].length, match.groupValues[2].trim())
            }.distinctBy { it.level to it.text.lowercase() }.toList()
        }.getOrDefault(emptyList())
    }

    fun logItem(config: QuickLoggerConfig, item: QuickLogItem, appendedText: String): LogUndoSnapshot {
        val rootUri = config.settings.logRootUri?.let(Uri::parse)
            ?: error("Pick a log root folder in Settings first.")
        val root = DocumentFile.fromTreeUri(context, rootUri)
            ?: error("The selected log root is no longer available.")

        val today = DailyPathPattern.effectiveDate(
            config.settings.dayStartHour,
            config.settings.dayStartMinute,
        )
        val resolved = resolveDailyFile(root, config.settings.dailyPathPattern, today)
        val file = resolved.file
        val existing = if (resolved.existed) readText(file.uri) else ""
        val base = existing.ifBlank {
            val templateUri = config.settings.templateUri?.let(Uri::parse)
                ?: error("Pick a daily note template in Settings first.")
            val title = file.name?.substringBeforeLast('.') ?: today.toString()
            SnippetInterpolator.interpolate(readText(templateUri), title)
        }

        val updated = insertLine(base, item, buildItemLine(item, appendedText), config.settings)
        writeText(file.uri, updated)
        return LogUndoSnapshot(
            fileUri = file.uri.toString(),
            fileExisted = resolved.existed,
            previousContent = existing,
        )
    }

    fun restore(snapshot: LogUndoSnapshot) {
        val uri = Uri.parse(snapshot.fileUri)
        if (snapshot.fileExisted) {
            writeText(uri, snapshot.previousContent)
            return
        }

        val file = DocumentFile.fromSingleUri(context, uri)
            ?: throw FileNotFoundException("Could not resolve file to undo.")
        if (!file.delete()) {
            throw FileNotFoundException("Could not delete newly-created daily note.")
        }
    }

    private fun resolveDailyFile(root: DocumentFile, pattern: String, date: LocalDate): ResolvedFile {
        val relativePath = DailyPathPattern.render(pattern, date)
        val parts = relativePath.split('/').map { it.trim() }.filter { it.isNotBlank() }
        require(parts.isNotEmpty()) { "Daily path pattern resolved to an empty path." }

        var directory = root
        parts.dropLast(1).forEach { segment ->
            directory = directory.findDirectory(segment)
                ?: directory.createDirectory(segment)
                ?: directory.findDirectory(segment)
                ?: throw FileNotFoundException("Could not create folder: $segment")
        }

        val fileName = parts.last()
        val existing = directory.findChildFile(fileName)
        if (existing != null) return ResolvedFile(existing, existed = true)

        val created = directory.createFile("text/markdown", fileName)
            ?: directory.findChildFile(fileName)
            ?: throw FileNotFoundException("Could not create daily note: $fileName")
        return ResolvedFile(created, existed = false)
    }

    private fun buildItemLine(item: QuickLogItem, appendedText: String): String {
        val bullet = when (item.bulletType) {
            BulletType.Ordered -> "1. "
            BulletType.Unordered -> "- "
            BulletType.None -> ""
        }
        val timestamp = when (item.timestampMode) {
            TimestampMode.None -> ""
            TimestampMode.Minutes -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " "
            TimestampMode.Seconds -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " "
        }
        val configuredText = item.insertText.ifBlank {
            if (item.showTextBox) "" else item.title
        }
        val body = listOf(configuredText, appendedText)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(" ")
        return bullet + timestamp + body
    }

    private fun insertLine(content: String, item: QuickLogItem, line: String, settings: AppSettings): String {
        val normalized = content.replace("\r\n", "\n")
        val lines = normalized.split('\n').toMutableList()
        if (lines.lastOrNull() == "") lines.removeAt(lines.lastIndex)

        val target = parseConfiguredHeading(item.heading)
        val headingIndex = lines.indexOfFirst { candidate ->
            val parsed = parseHeading(candidate) ?: return@indexOfFirst false
            val textMatches = if (item.matchCase) {
                parsed.text == target.text
            } else {
                parsed.text.equals(target.text, ignoreCase = true)
            }
            textMatches && (target.level == null || parsed.level == target.level)
        }

        if (headingIndex == -1) {
            if (lines.isNotEmpty() && lines.last().isNotBlank()) lines.add("")
            lines.add("${"#".repeat(target.level ?: 2)} ${target.text}")
            if (item.subheadingMode == SubheadingMode.Time) {
                val subheadingLevel = ((target.level ?: 2) + 1).coerceAtMost(6)
                lines.add("${"#".repeat(subheadingLevel)} ${timeSubheadingLabel(item.timeSubheading, settings)}")
            }
            lines.add(line)
            return lines.joinToString("\n") + "\n"
        }

        val headingLevel = parseHeading(lines[headingIndex])?.level ?: 2
        val sectionEnd = lines.indexOfFirstAfter(headingIndex + 1) { candidate ->
            val parsed = parseHeading(candidate)
            parsed?.level != null && parsed.level <= headingLevel
        }.let { if (it == -1) lines.size else it }

        val sizeBeforeSubheading = lines.size
        val insertionHeadingIndex = if (item.subheadingMode == SubheadingMode.Time) {
            resolveTimeSubheading(lines, headingIndex, headingLevel, sectionEnd, item, settings)
        } else {
            headingIndex
        }
        val adjustedSectionEnd = sectionEnd + (lines.size - sizeBeforeSubheading)
        val insertionHeadingLevel = parseHeading(lines[insertionHeadingIndex])?.level ?: headingLevel
        val insertionSectionEnd = lines.indexOfFirstAfter(insertionHeadingIndex + 1) { candidate ->
            val parsed = parseHeading(candidate)
            parsed?.level != null && parsed.level <= insertionHeadingLevel
        }.let { if (it == -1) adjustedSectionEnd else minOf(it, adjustedSectionEnd) }

        val bottomInsertAt = previousNonBlankIndex(lines, insertionSectionEnd - 1) + 1
        val insertAt = when (item.insertPosition) {
            InsertPosition.Top -> insertionHeadingIndex + 1
            InsertPosition.Bottom -> bottomInsertAt.coerceAtLeast(insertionHeadingIndex + 1)
        }
        lines.add(insertAt, line)
        return lines.joinToString("\n") + "\n"
    }

    private fun resolveTimeSubheading(
        lines: MutableList<String>,
        parentIndex: Int,
        parentLevel: Int,
        parentEnd: Int,
        item: QuickLogItem,
        settings: AppSettings,
    ): Int {
        val label = timeSubheadingLabel(item.timeSubheading, settings)
        val configuredLevel = (parentLevel + 1).coerceAtMost(6)
        val existingIndex = (parentIndex + 1 until parentEnd).firstOrNull { index ->
            val parsed = parseHeading(lines[index]) ?: return@firstOrNull false
            parsed.level == configuredLevel && parsed.text.equals(label, ignoreCase = true)
        }
        if (existingIndex != null || !item.timeSubheading.createMissing) {
            return existingIndex ?: parentIndex
        }

        val insertAt = (previousNonBlankIndex(lines, parentEnd - 1) + 1).coerceAtLeast(parentIndex + 1)
        lines.add(insertAt, "${"#".repeat(configuredLevel)} $label")
        return insertAt
    }

    private fun timeSubheadingLabel(config: TimeSubheadingConfig, settings: AppSettings): String {
        val now = LocalTime.now()
        val current = now.hour * 60 + now.minute
        val dayStart = settings.dayStartHour * 60 + settings.dayStartMinute
        val first = config.firstHour * 60 + config.firstMinute
        val last = config.lastHour * 60 + config.lastMinute
        if (current < dayStart) return config.lateLabel
        if (current < first) return config.earlyLabel

        val bucket = first + ((current - first) / config.intervalMinutes) * config.intervalMinutes
        if (bucket > last) return config.lateLabel
        return "%02d:%02d".format(bucket / 60, bucket % 60)
    }

    private fun parseConfiguredHeading(raw: String): ParsedHeading {
        val parsed = parseHeading(raw)
        return parsed ?: ParsedHeading(null, raw.trim().ifBlank { "Log" })
    }

    private fun parseHeading(line: String): ParsedHeading? {
        val match = headingRegex.matchEntire(line.trim()) ?: return null
        return ParsedHeading(match.groupValues[1].length, match.groupValues[2].trim())
    }

    private inline fun List<String>.indexOfFirstAfter(start: Int, predicate: (String) -> Boolean): Int {
        for (index in start until size) {
            if (predicate(this[index])) return index
        }
        return -1
    }

    private fun previousNonBlankIndex(lines: List<String>, start: Int): Int {
        for (index in start downTo 0) {
            if (lines[index].isNotBlank()) return index
        }
        return -1
    }

    private fun readText(uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw FileNotFoundException("Could not read $uri")

    private fun writeText(uri: Uri, text: String) {
        context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(text) }
            ?: throw FileNotFoundException("Could not write $uri")
    }

    private fun DocumentFile.findDirectory(name: String): DocumentFile? =
        findFile(name)?.takeIf { it.isDirectory }
            ?: listFiles().firstOrNull { it.name == name && it.isDirectory }

    private fun DocumentFile.findChildFile(name: String): DocumentFile? =
        findFile(name)?.takeIf { it.isFile }
            ?: listFiles().firstOrNull { it.name == name && it.isFile }

    private data class ParsedHeading(
        val level: Int?,
        val text: String,
    )

    private data class ResolvedFile(
        val file: DocumentFile,
        val existed: Boolean,
    )
}

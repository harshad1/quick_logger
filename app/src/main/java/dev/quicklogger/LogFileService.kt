package dev.quicklogger

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

    fun logItem(config: QuickLoggerConfig, item: QuickLogItem, appendedText: String) {
        val rootUri = config.settings.logRootUri?.let(Uri::parse)
            ?: error("Pick a log root folder in Settings first.")
        val root = DocumentFile.fromTreeUri(context, rootUri)
            ?: error("The selected log root is no longer available.")

        val today = DailyPathPattern.effectiveDate(config.settings.dayBoundaryDelayMinutes)
        val file = resolveDailyFile(root, config.settings.dailyPathPattern, today)
        val existing = if (file.length() > 0) readText(file.uri) else ""
        val base = existing.ifBlank {
            val templateUri = config.settings.templateUri?.let(Uri::parse)
                ?: error("Pick a daily note template in Settings first.")
            val title = file.name?.substringBeforeLast('.') ?: today.toString()
            SnippetInterpolator.interpolate(readText(templateUri), title)
        }

        val updated = insertLine(base, item, buildItemLine(item, appendedText))
        writeText(file.uri, updated)
    }

    private fun resolveDailyFile(root: DocumentFile, pattern: String, date: LocalDate): DocumentFile {
        val relativePath = DailyPathPattern.render(pattern, date)
        val parts = relativePath.split('/').map { it.trim() }.filter { it.isNotBlank() }
        require(parts.isNotEmpty()) { "Daily path pattern resolved to an empty path." }

        var directory = root
        parts.dropLast(1).forEach { segment ->
            directory = directory.findFile(segment)?.takeIf { it.isDirectory }
                ?: directory.createDirectory(segment)
                ?: throw FileNotFoundException("Could not create folder: $segment")
        }

        val fileName = parts.last()
        return directory.findFile(fileName)?.takeIf { it.isFile }
            ?: directory.createFile("text/markdown", fileName)
            ?: throw FileNotFoundException("Could not create daily note: $fileName")
    }

    private fun buildItemLine(item: QuickLogItem, appendedText: String): String {
        val bullet = when (item.bulletType) {
            BulletType.Ordered -> "1. "
            BulletType.Unordered -> "- "
            BulletType.None -> ""
        }
        val timestamp = if (item.addTimestamp) {
            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " "
        } else {
            ""
        }
        val body = listOf(item.insertText.ifBlank { item.title }, appendedText)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(" ")
        return bullet + timestamp + body
    }

    private fun insertLine(content: String, item: QuickLogItem, line: String): String {
        val normalized = content.replace("\r\n", "\n")
        val lines = normalized.split('\n').toMutableList()
        if (lines.lastOrNull() == "") lines.removeAt(lines.lastIndex)

        val target = parseConfiguredHeading(item.heading, item.headingLevel)
        val headingIndex = lines.indexOfFirst { candidate ->
            val parsed = parseHeading(candidate) ?: return@indexOfFirst false
            val textMatches = if (item.matchCase) {
                parsed.text == target.text
            } else {
                parsed.text.equals(target.text, ignoreCase = true)
            }
            textMatches && (!item.matchHeadingLevel || parsed.level == target.level)
        }

        if (headingIndex == -1) {
            if (lines.isNotEmpty() && lines.last().isNotBlank()) lines.add("")
            lines.add("${"#".repeat(target.level)} ${target.text}")
            lines.add(line)
            return lines.joinToString("\n") + "\n"
        }

        val headingLevel = parseHeading(lines[headingIndex])?.level ?: target.level
        val sectionEnd = lines.indexOfFirstAfter(headingIndex + 1) { candidate ->
            val parsed = parseHeading(candidate)
            parsed != null && parsed.level <= headingLevel
        }.let { if (it == -1) lines.size else it }

        val bottomInsertAt = previousNonBlankIndex(lines, sectionEnd - 1) + 1
        val insertAt = when (item.insertPosition) {
            InsertPosition.Top -> headingIndex + 1
            InsertPosition.Bottom -> bottomInsertAt.coerceAtLeast(headingIndex + 1)
        }
        lines.add(insertAt, line)
        return lines.joinToString("\n") + "\n"
    }

    private fun parseConfiguredHeading(raw: String, fallbackLevel: Int): ParsedHeading {
        val parsed = parseHeading(raw)
        return parsed ?: ParsedHeading(fallbackLevel.coerceIn(1, 6), raw.trim().ifBlank { "Log" })
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

    private data class ParsedHeading(
        val level: Int,
        val text: String,
    )
}

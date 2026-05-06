package dev.quicklogger

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException
import java.time.LocalDate

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

        val updated = DailyLogFormatter.insertItem(base, item, appendedText, config.settings)
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

    private fun parseHeading(line: String): ParsedHeading? {
        val match = headingRegex.matchEntire(line.trim()) ?: return null
        return ParsedHeading(match.groupValues[1].length, match.groupValues[2].trim())
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

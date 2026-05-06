package dev.quicklogger

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object DailyLogFormatter {
    private val headingRegex = Regex("^(#{1,6})\\s+(.+?)\\s*#*\\s*$")

    fun insertItem(
        content: String,
        item: QuickLogItem,
        appendedText: String,
        settings: AppSettings,
        now: LocalTime = LocalTime.now(),
    ): String = insertLine(content, item, buildItemLine(item, appendedText, now), settings, now)

    private fun buildItemLine(item: QuickLogItem, appendedText: String, now: LocalTime): String {
        val bullet = when (item.bulletType) {
            BulletType.Ordered -> "1. "
            BulletType.Unordered -> "- "
            BulletType.None -> ""
        }
        val timestamp = when (item.timestampMode) {
            TimestampMode.None -> ""
            TimestampMode.Minutes -> now.format(DateTimeFormatter.ofPattern("HH:mm")) + " "
            TimestampMode.Seconds -> now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " "
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

    private fun insertLine(
        content: String,
        item: QuickLogItem,
        line: String,
        settings: AppSettings,
        now: LocalTime,
    ): String {
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
                lines.add("${"#".repeat(subheadingLevel)} ${timeSubheadingLabel(item.timeSubheading, settings, now)}")
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
            resolveTimeSubheading(lines, headingIndex, headingLevel, sectionEnd, item, settings, now)
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
        now: LocalTime,
    ): Int {
        val label = timeSubheadingLabel(item.timeSubheading, settings, now)
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

    private fun timeSubheadingLabel(config: TimeSubheadingConfig, settings: AppSettings, now: LocalTime): String {
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

    private data class ParsedHeading(
        val level: Int?,
        val text: String,
    )
}

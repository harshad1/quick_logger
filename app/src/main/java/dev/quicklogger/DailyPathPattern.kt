package dev.quicklogger

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DailyPathPattern {
    fun effectiveDate(dayStartHour: Int, dayStartMinute: Int, now: LocalDateTime = LocalDateTime.now()): LocalDate {
        val start = now.toLocalDate().atTime(dayStartHour.coerceIn(0, 23), dayStartMinute.coerceIn(0, 59))
        return if (now.isBefore(start)) now.toLocalDate().minusDays(1) else now.toLocalDate()
    }

    fun render(pattern: String, date: LocalDate): String {
        val year = date.format(DateTimeFormatter.ofPattern("yyyy", Locale.US))
        val month = date.format(DateTimeFormatter.ofPattern("MM", Locale.US))
        val day = date.format(DateTimeFormatter.ofPattern("dd", Locale.US))
        return pattern
            .replace("yyyy", year)
            .replace("YYYY", year)
            .replace("MM", month)
            .replace("mm", month)
            .replace("dd", day)
            .replace("DD", day)
    }
}

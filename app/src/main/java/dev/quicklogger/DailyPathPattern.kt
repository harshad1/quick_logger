package dev.quicklogger

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DailyPathPattern {
    fun effectiveDate(delayMinutes: Int, now: LocalDateTime = LocalDateTime.now()): LocalDate =
        now.minusMinutes(delayMinutes.coerceAtLeast(0).toLong()).toLocalDate()

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

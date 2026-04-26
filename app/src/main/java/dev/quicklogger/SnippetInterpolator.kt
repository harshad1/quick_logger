package dev.quicklogger

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object SnippetInterpolator {
    fun interpolate(text: String, title: CharSequence, selectedText: CharSequence = ""): String {
        val current = Date()
        var output = text
            .replace("{{time}}", format("HH:mm", current))
            .replace("{{date}}", format("yyyy-MM-dd", current))
            .replace("{{title}}", title.toString())
            .replace("{{weekday}}", format("EEEE", current))
            .replace("{{sel}}", selectedText.toString())
            .replace("{{cursor}}", "")

        while (output.contains("{{uuid}}")) {
            output = output.replaceFirst("{{uuid}}", UUID.randomUUID().toString())
        }

        return interpolateEscapedDateTime(output, current)
    }

    private fun interpolateEscapedDateTime(text: String, current: Date): String {
        val interpolated = StringBuilder()
        val temp = StringBuilder()
        var isEscaped = false
        var inDate = false

        for (char in text) {
            when {
                char == '\\' && !isEscaped -> isEscaped = true
                isEscaped -> {
                    isEscaped = false
                    temp.append(char)
                }
                char == '`' && inDate -> {
                    inDate = false
                    interpolated.append(format(temp.toString(), current))
                    temp.clear()
                }
                char == '`' -> {
                    inDate = true
                    interpolated.append(temp)
                    temp.clear()
                }
                else -> temp.append(char)
            }
        }

        if (inDate) interpolated.append('`')
        interpolated.append(temp)
        return interpolated.toString()
    }

    private fun format(pattern: String, date: Date): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}

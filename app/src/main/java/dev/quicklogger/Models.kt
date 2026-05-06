package dev.quicklogger

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class InsertPosition { Top, Bottom }
enum class BulletType { Ordered, Unordered, None }
enum class ThemeMode { System, Light, Dark }
enum class TimestampMode { None, Minutes, Seconds }
enum class SubheadingMode { None, Time }

data class QuickLoggerConfig(
    val settings: AppSettings = AppSettings(),
    val items: List<QuickLogItem> = emptyList(),
) {
    fun toJson(): String {
        val root = JSONObject()
            .put("version", 1)
            .put("settings", settings.toJsonObject())
            .put("items", JSONArray(items.map { it.toJsonObject() }))
        return root.toString(2)
    }

    companion object {
        fun fromJson(text: String): QuickLoggerConfig {
            if (text.isBlank()) return QuickLoggerConfig()
            val root = JSONObject(text)
            val itemsJson = root.optJSONArray("items") ?: JSONArray()
            return QuickLoggerConfig(
                settings = AppSettings.fromJsonObject(root.optJSONObject("settings")),
                items = List(itemsJson.length()) { index ->
                    QuickLogItem.fromJsonObject(itemsJson.getJSONObject(index))
                },
            )
        }
    }
}

data class AppSettings(
    val logRootUri: String? = null,
    val templateUri: String? = null,
    val dailyPathPattern: String = "yyyy/yyyy-MM/yyyy-MM-dd.md",
    val dayStartHour: Int = 0,
    val dayStartMinute: Int = 0,
    val themeMode: ThemeMode = ThemeMode.System,
) {
    companion object {
        fun fromJsonObject(json: JSONObject?): AppSettings {
            val legacyDelay = json?.optInt("dayBoundaryDelayMinutes", 0)?.coerceAtLeast(0) ?: 0
            return AppSettings(
                logRootUri = json?.optNullableString("logRootUri"),
                templateUri = json?.optNullableString("templateUri"),
                dailyPathPattern = json?.optString("dailyPathPattern", "yyyy/yyyy-MM/yyyy-MM-dd.md")
                    ?: "yyyy/yyyy-MM/yyyy-MM-dd.md",
                dayStartHour = json?.optInt("dayStartHour", legacyDelay / 60)?.coerceIn(0, 23) ?: 0,
                dayStartMinute = json?.optInt("dayStartMinute", legacyDelay % 60)?.coerceIn(0, 59) ?: 0,
                themeMode = enumValueOrDefault(json?.optString("themeMode").orEmpty(), ThemeMode.System),
            )
        }
    }
}

data class QuickLogItem(
    val id: String = UUID.randomUUID().toString(),
    val heading: String = "Consumption",
    val matchCase: Boolean = false,
    val insertPosition: InsertPosition = InsertPosition.Bottom,
    val title: String = "",
    val insertText: String = "",
    val icon: String = "edit",
    val timestampMode: TimestampMode = TimestampMode.Minutes,
    val bulletType: BulletType = BulletType.Ordered,
    val showTextBox: Boolean = false,
    val subheadingMode: SubheadingMode = SubheadingMode.None,
    val timeSubheading: TimeSubheadingConfig = TimeSubheadingConfig(),
) {
    companion object {
        fun fromJsonObject(json: JSONObject): QuickLogItem = QuickLogItem(
            id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
            heading = json.optString("heading", "Consumption"),
            matchCase = json.optBoolean("matchCase", false),
            insertPosition = enumValueOrDefault(json.optString("insertPosition"), InsertPosition.Bottom),
            title = json.optString("title", ""),
            insertText = if (json.has("insertText")) {
                json.optString("insertText", "")
            } else {
                json.optString("title", "")
            },
            icon = json.optString("icon", "edit"),
            timestampMode = if (json.has("timestampMode")) {
                enumValueOrDefault(json.optString("timestampMode"), TimestampMode.Minutes)
            } else if (json.optBoolean("addTimestamp", true)) {
                TimestampMode.Minutes
            } else {
                TimestampMode.None
            },
            bulletType = enumValueOrDefault(json.optString("bulletType"), BulletType.Ordered),
            showTextBox = json.optBoolean("showTextBox", false),
            subheadingMode = enumValueOrDefault(json.optString("subheadingMode"), SubheadingMode.None),
            timeSubheading = TimeSubheadingConfig.fromJsonObject(json.optJSONObject("timeSubheading")),
        )
    }
}

data class TimeSubheadingConfig(
    val firstHour: Int = 7,
    val firstMinute: Int = 0,
    val lastHour: Int = 23,
    val lastMinute: Int = 0,
    val intervalMinutes: Int = 60,
    val earlyLabel: String = "Early",
    val lateLabel: String = "Late",
    val createMissing: Boolean = true,
) {
    companion object {
        fun fromJsonObject(json: JSONObject?): TimeSubheadingConfig = TimeSubheadingConfig(
            firstHour = json?.optInt("firstHour", 7)?.coerceIn(0, 23) ?: 7,
            firstMinute = json?.optInt("firstMinute", 0)?.coerceIn(0, 59) ?: 0,
            lastHour = json?.optInt("lastHour", 23)?.coerceIn(0, 23) ?: 23,
            lastMinute = json?.optInt("lastMinute", 0)?.coerceIn(0, 59) ?: 0,
            intervalMinutes = json?.optInt("intervalMinutes", 60)?.coerceAtLeast(1) ?: 60,
            earlyLabel = json?.optString("earlyLabel", "Early")?.ifBlank { "Early" } ?: "Early",
            lateLabel = json?.optString("lateLabel", "Late")?.ifBlank { "Late" } ?: "Late",
            createMissing = json?.optBoolean("createMissing", true) ?: true,
        )
    }
}

data class HeadingSuggestion(
    val level: Int,
    val text: String,
) {
    val label: String = "${"#".repeat(level)} $text"
}

fun AppSettings.toJsonObject(): JSONObject = JSONObject()
    .put("logRootUri", logRootUri)
    .put("templateUri", templateUri)
    .put("dailyPathPattern", dailyPathPattern)
    .put("dayStartHour", dayStartHour)
    .put("dayStartMinute", dayStartMinute)
    .put("themeMode", themeMode.name)

fun QuickLogItem.toJsonObject(): JSONObject = JSONObject()
    .put("id", id)
    .put("heading", heading)
    .put("matchCase", matchCase)
    .put("insertPosition", insertPosition.name)
    .put("title", title)
    .put("insertText", insertText)
    .put("icon", icon)
    .put("timestampMode", timestampMode.name)
    .put("bulletType", bulletType.name)
    .put("showTextBox", showTextBox)
    .put("subheadingMode", subheadingMode.name)
    .put("timeSubheading", timeSubheading.toJsonObject())

fun TimeSubheadingConfig.toJsonObject(): JSONObject = JSONObject()
    .put("firstHour", firstHour)
    .put("firstMinute", firstMinute)
    .put("lastHour", lastHour)
    .put("lastMinute", lastMinute)
    .put("intervalMinutes", intervalMinutes)
    .put("earlyLabel", earlyLabel)
    .put("lateLabel", lateLabel)
    .put("createMissing", createMissing)

private fun JSONObject.optNullableString(name: String): String? =
    if (has(name) && !isNull(name)) optString(name) else null

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback

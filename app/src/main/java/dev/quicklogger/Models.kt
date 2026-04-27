package dev.quicklogger

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class InsertPosition { Top, Bottom }
enum class BulletType { Ordered, Unordered, None }
enum class ThemeMode { System, Light, Dark }

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
    val dayBoundaryDelayMinutes: Int = 0,
    val themeMode: ThemeMode = ThemeMode.System,
) {
    companion object {
        fun fromJsonObject(json: JSONObject?): AppSettings = AppSettings(
            logRootUri = json?.optNullableString("logRootUri"),
            templateUri = json?.optNullableString("templateUri"),
            dailyPathPattern = json?.optString("dailyPathPattern", "yyyy/yyyy-MM/yyyy-MM-dd.md")
                ?: "yyyy/yyyy-MM/yyyy-MM-dd.md",
            dayBoundaryDelayMinutes = json?.optInt("dayBoundaryDelayMinutes", 0)?.coerceAtLeast(0) ?: 0,
            themeMode = enumValueOrDefault(json?.optString("themeMode").orEmpty(), ThemeMode.System),
        )
    }
}

data class QuickLogItem(
    val id: String = UUID.randomUUID().toString(),
    val heading: String = "Consumption",
    val headingLevel: Int = 1,
    val matchCase: Boolean = false,
    val matchHeadingLevel: Boolean = false,
    val insertPosition: InsertPosition = InsertPosition.Bottom,
    val title: String = "",
    val insertText: String = "",
    val icon: String = "edit",
    val addTimestamp: Boolean = true,
    val bulletType: BulletType = BulletType.Ordered,
    val showTextBox: Boolean = false,
) {
    companion object {
        fun fromJsonObject(json: JSONObject): QuickLogItem = QuickLogItem(
            id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
            heading = json.optString("heading", "Consumption"),
            headingLevel = json.optInt("headingLevel", 1).coerceIn(1, 6),
            matchCase = json.optBoolean("matchCase", false),
            matchHeadingLevel = json.optBoolean("matchHeadingLevel", false),
            insertPosition = enumValueOrDefault(json.optString("insertPosition"), InsertPosition.Bottom),
            title = json.optString("title", ""),
            insertText = json.optString("insertText").ifBlank { json.optString("title", "") },
            icon = json.optString("icon", "edit"),
            addTimestamp = json.optBoolean("addTimestamp", true),
            bulletType = enumValueOrDefault(json.optString("bulletType"), BulletType.Ordered),
            showTextBox = json.optBoolean("showTextBox", false),
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
    .put("dayBoundaryDelayMinutes", dayBoundaryDelayMinutes)
    .put("themeMode", themeMode.name)

fun QuickLogItem.toJsonObject(): JSONObject = JSONObject()
    .put("id", id)
    .put("heading", heading)
    .put("headingLevel", headingLevel)
    .put("matchCase", matchCase)
    .put("matchHeadingLevel", matchHeadingLevel)
    .put("insertPosition", insertPosition.name)
    .put("title", title)
    .put("insertText", insertText)
    .put("icon", icon)
    .put("addTimestamp", addTimestamp)
    .put("bulletType", bulletType.name)
    .put("showTextBox", showTextBox)

private fun JSONObject.optNullableString(name: String): String? =
    if (has(name) && !isNull(name)) optString(name) else null

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback

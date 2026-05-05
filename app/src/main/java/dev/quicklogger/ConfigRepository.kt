package dev.quicklogger

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.configDataStore by preferencesDataStore(name = "quick_logger")

class ConfigRepository(private val context: Context) {
    private val configKey = stringPreferencesKey("config_json")
    private val logFileService = LogFileService(context)

    val configFlow: Flow<QuickLoggerConfig> = context.configDataStore.data.map { preferences ->
        runCatching {
            QuickLoggerConfig.fromJson(preferences[configKey].orEmpty())
        }.getOrDefault(QuickLoggerConfig())
    }

    suspend fun currentConfig(): QuickLoggerConfig = configFlow.first()

    suspend fun saveSettings(settings: AppSettings) {
        updateConfig { it.copy(settings = settings) }
    }

    suspend fun saveItem(item: QuickLogItem) {
        updateConfig { config ->
            val updated = config.items.filterNot { it.id == item.id } + item
            config.copy(items = updated)
        }
    }

    suspend fun reorderItems(items: List<QuickLogItem>) {
        updateConfig { config ->
            val reorderedIds = items.map { it.id }.toSet()
            config.copy(items = items + config.items.filterNot { it.id in reorderedIds })
        }
    }

    suspend fun deleteItem(itemId: String) {
        updateConfig { config ->
            config.copy(items = config.items.filterNot { it.id == itemId })
        }
    }

    suspend fun importJson(json: String) {
        val imported = QuickLoggerConfig.fromJson(json)
        context.configDataStore.edit { preferences ->
            preferences[configKey] = imported.toJson()
        }
    }

    suspend fun exportJson(): String = currentConfig().toJson()

    suspend fun logItem(item: QuickLogItem, appendedText: String): LogUndoSnapshot =
        logFileService.logItem(currentConfig(), item, appendedText)

    suspend fun undoLog(snapshot: LogUndoSnapshot) {
        logFileService.restore(snapshot)
    }

    suspend fun readTemplateHeadings(): List<HeadingSuggestion> =
        logFileService.readTemplateHeadings(currentConfig().settings.templateUri?.let(Uri::parse))
            .ifEmpty { DefaultHeadingSuggestions }

    private suspend fun updateConfig(transform: (QuickLoggerConfig) -> QuickLoggerConfig) {
        context.configDataStore.edit { preferences ->
            val current = runCatching {
                QuickLoggerConfig.fromJson(preferences[configKey].orEmpty())
            }.getOrDefault(QuickLoggerConfig())
            preferences[configKey] = transform(current).toJson()
        }
    }
}

private val DefaultHeadingSuggestions = listOf(
    HeadingSuggestion(1, "Intention"),
    HeadingSuggestion(2, "Habits"),
    HeadingSuggestion(1, "Log:"),
    HeadingSuggestion(2, "Consumption"),
    HeadingSuggestion(2, "State"),
    HeadingSuggestion(2, "Media"),
    HeadingSuggestion(1, "Review:"),
    HeadingSuggestion(1, "Notes"),
)

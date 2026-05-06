package dev.quicklogger

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun SettingsScreen(
    settings: AppSettings,
    repository: ConfigRepository,
    onSaved: (AppSettings) -> Unit,
    onMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    var draft by remember(settings) { mutableStateOf(settings) }
    val scope = rememberCoroutineScope()
    fun updateSettings(updated: AppSettings) {
        draft = updated
        onSaved(updated)
    }

    val rootPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        updateSettings(draft.copy(logRootUri = uri.toString()))
    }

    val templatePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        updateSettings(draft.copy(templateUri = uri.toString()))
    }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use {
                    it.write(repository.exportJson())
                } ?: error("Could not open export file.")
            }.onSuccess {
                onMessage("Exported configuration")
            }.onFailure {
                onMessage(it.message ?: "Export failed")
            }
        }
    }

    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Could not read import file.")
                repository.importJson(json)
            }.onSuccess {
                onMessage("Imported configuration")
            }.onFailure {
                onMessage(it.message ?: "Import failed")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsCard(title = "Daily notes") {
            Button(onClick = { rootPicker.launch(null) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (draft.logRootUri == null) "Pick log root" else "Change log root")
            }
            UriPreview(draft.logRootUri, tree = true)
            Button(onClick = { templatePicker.launch(arrayOf("text/*", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.TextSnippet, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (draft.templateUri == null) "Pick template" else "Change template")
            }
            UriPreview(draft.templateUri, tree = false)
            OutlinedTextField(
                value = draft.dailyPathPattern,
                onValueChange = { updateSettings(draft.copy(dailyPathPattern = it)) },
                label = { Text("Daily path pattern") },
                supportingText = {
                    Text(
                        "Today: ${
                            DailyPathPattern.render(
                                draft.dailyPathPattern,
                                DailyPathPattern.effectiveDate(draft.dayStartHour, draft.dayStartMinute),
                            )
                        }",
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Text("Day starts at", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallDropdown(
                    value = "%02d".format(draft.dayStartHour),
                    options = (0..23).map { "%02d".format(it) },
                    onSelected = { updateSettings(draft.copy(dayStartHour = it.toInt())) },
                    modifier = Modifier.weight(1f),
                )
                SmallDropdown(
                    value = "%02d".format(draft.dayStartMinute),
                    options = (0..59).map { "%02d".format(it) },
                    onSelected = { updateSettings(draft.copy(dayStartMinute = it.toInt())) },
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                "Before this time, logs still go to the previous daily file.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
            )
        }

        SettingsCard(title = "Appearance") {
            Text("Theme", fontWeight = FontWeight.Bold)
            FlowRowCompat(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = draft.themeMode == mode,
                        onClick = { updateSettings(draft.copy(themeMode = mode)) },
                        label = { Text(mode.name) },
                    )
                }
            }
        }

        SettingsCard(title = "Backup") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { exporter.launch("quick-logger-config.json") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export")
                }
                OutlinedButton(
                    onClick = { importer.launch(arrayOf("application/json", "text/*")) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Import")
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp)
            content()
        }
    }
}

@Composable
private fun UriPreview(uri: String?, tree: Boolean) {
    val context = LocalContext.current
    if (uri != null) {
        Text(
            readableUri(context, uri, tree),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun readableUri(context: Context, uriString: String, tree: Boolean): String {
    val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return uriString
    if (tree) {
        val treeId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
        if (!treeId.isNullOrBlank()) {
            val decoded = URLDecoder.decode(treeId, StandardCharsets.UTF_8.name())
            val volume = decoded.substringBefore(':')
            val relative = decoded.substringAfter(':', "")
            return when {
                volume == "primary" && relative.isBlank() -> "/storage/emulated/0"
                volume == "primary" -> "/storage/emulated/0/$relative"
                relative.isBlank() -> "$volume:"
                else -> "$volume:/$relative"
            }
        }
    }

    val displayName = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
    }.getOrNull()

    return displayName ?: uri.lastPathSegment ?: uriString
}

package dev.quicklogger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    private val repository by lazy { ConfigRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val config by repository.configFlow.collectAsState(initial = QuickLoggerConfig())
            QuickLoggerTheme(config.settings.themeMode) {
                QuickLoggerApp(repository, config)
            }
        }
    }
}

private sealed interface Screen {
    data object Home : Screen
    data object Settings : Screen
    data class EditItem(val item: QuickLogItem?) : Screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickLoggerApp(repository: ConfigRepository, config: QuickLoggerConfig) {
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var textItem by remember { mutableStateOf<QuickLogItem?>(null) }
    val darkTheme = shouldUseDarkTheme(config.settings.themeMode)

    BackHandler(screen !is Screen.Home) { screen = Screen.Home }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
                title = {
                    Text(
                        text = when (screen) {
                            Screen.Home -> "Quick Logger"
                            Screen.Settings -> "Settings"
                            is Screen.EditItem -> "Log Item"
                        },
                        fontWeight = FontWeight.Black,
                    )
                },
                navigationIcon = {
                    if (screen !is Screen.Home) {
                        IconButton(onClick = { screen = Screen.Home }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (screen is Screen.Home) {
                        IconButton(onClick = { screen = Screen.EditItem(null) }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add item")
                        }
                        IconButton(onClick = { screen = Screen.Settings }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackground(darkTheme))
                .padding(padding),
        ) {
            when (val current = screen) {
                Screen.Home -> HomeScreen(
                    config = config,
                    onItemClick = { item ->
                        if (item.showTextBox) {
                            textItem = item
                        } else {
                            scope.launch {
                                runCatching { repository.logItem(item, "") }
                                    .onSuccess { activity?.finish() }
                                    .onFailure { snackbarHostState.showSnackbar(it.message ?: "Could not write log item.") }
                            }
                        }
                    },
                    onItemLongClick = { screen = Screen.EditItem(it) },
                    onSettingsClick = { screen = Screen.Settings },
                    onAddClick = { screen = Screen.EditItem(null) },
                )
                Screen.Settings -> SettingsScreen(
                    settings = config.settings,
                    repository = repository,
                    onSaved = { settings ->
                        scope.launch {
                            repository.saveSettings(settings)
                        }
                    },
                    onMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    },
                )
                is Screen.EditItem -> ItemEditorScreen(
                    initial = current.item,
                    repository = repository,
                    onSave = { item ->
                        scope.launch {
                            repository.saveItem(item)
                            screen = Screen.Home
                        }
                    },
                    onDelete = current.item?.let { item ->
                        {
                            scope.launch {
                                repository.deleteItem(item.id)
                                screen = Screen.Home
                            }
                        }
                    },
                )
            }
        }
    }

    textItem?.let { item ->
        TextEntryDialog(
            item = item,
            onDismiss = { textItem = null },
            onSubmit = { text ->
                textItem = null
                scope.launch {
                    runCatching { repository.logItem(item, text) }
                        .onSuccess { activity?.finish() }
                        .onFailure { snackbarHostState.showSnackbar(it.message ?: "Could not write log item.") }
                }
            },
        )
    }
}

@Composable
private fun HomeScreen(
    config: QuickLoggerConfig,
    onItemClick: (QuickLogItem) -> Unit,
    onItemLongClick: (QuickLogItem) -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
    ) {
        if (config.settings.logRootUri == null || config.settings.templateUri == null) {
            SetupCard(onSettingsClick)
        }
        if (config.items.isEmpty()) {
            EmptyState(onAddClick)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(108.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(config.items, key = { it.id }) { item ->
                    QuickLogTile(
                        item = item,
                        onClick = { onItemClick(item) },
                        onLongClick = { onItemLongClick(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupCard(onSettingsClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 10.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Setup needed", fontWeight = FontWeight.Bold)
                Text("Pick a log root and template before logging.", color = MaterialTheme.colorScheme.secondary)
            }
            OutlinedButton(onClick = onSettingsClick) {
                Text("Settings")
            }
        }
    }
}

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No quick items yet", fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text("Create items for Consumption, State, Media, or any heading in your daily note.")
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Create item")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickLogTile(item: QuickLogItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .height(112.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = iconFor(item.icon).vector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column {
                Text(
                    item.title.ifBlank { "Untitled" },
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    item.heading,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    repository: ConfigRepository,
    onSaved: (AppSettings) -> Unit,
    onMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    var draft by remember(settings) { mutableStateOf(settings) }
    var delayText by remember(settings.dayBoundaryDelayMinutes) {
        mutableStateOf(settings.dayBoundaryDelayMinutes.toString())
    }
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
        val updated = draft.copy(logRootUri = uri.toString())
        updateSettings(updated)
    }

    val templatePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val updated = draft.copy(templateUri = uri.toString())
        updateSettings(updated)
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
                Icon(Icons.Filled.TextSnippet, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (draft.templateUri == null) "Pick template" else "Change template")
            }
            UriPreview(draft.templateUri, tree = false)
            OutlinedTextField(
                value = draft.dailyPathPattern,
                onValueChange = { updateSettings(draft.copy(dailyPathPattern = it)) },
                label = { Text("Daily path pattern") },
                supportingText = {
                    Text("Today: ${DailyPathPattern.render(draft.dailyPathPattern, DailyPathPattern.effectiveDate(draft.dayBoundaryDelayMinutes))}")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = delayText,
                onValueChange = { value ->
                    if (value.all(Char::isDigit)) {
                        delayText = value
                        updateSettings(draft.copy(dayBoundaryDelayMinutes = value.toIntOrNull() ?: 0))
                    }
                },
                label = { Text("Day boundary delay, minutes") },
                supportingText = { Text("Used only for choosing the daily file after midnight.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
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

@Composable
private fun ItemEditorScreen(
    initial: QuickLogItem?,
    repository: ConfigRepository,
    onSave: (QuickLogItem) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var item by remember(initial?.id) { mutableStateOf(initial ?: QuickLogItem()) }
    var iconPickerOpen by remember { mutableStateOf(false) }
    var suggestionsOpen by remember { mutableStateOf(false) }
    var headingSuggestions by remember { mutableStateOf(emptyList<HeadingSuggestion>()) }

    LaunchedEffect(Unit) {
        headingSuggestions = repository.readTemplateHeadings()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .clickable { iconPickerOpen = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconFor(item.icon).vector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Icon", fontWeight = FontWeight.Bold)
                Text("Tap to choose a flat Material symbol.", color = MaterialTheme.colorScheme.secondary)
            }
        }

        OutlinedTextField(
            value = item.title,
            onValueChange = { item = item.copy(title = it) },
            label = { Text("Display title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = item.insertText,
            onValueChange = { item = item.copy(insertText = it) },
            label = { Text("Text to insert") },
            supportingText = { Text("Example: 200mg ibuprofen") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = item.heading,
                onValueChange = { item = item.copy(heading = it) },
                label = { Text("Heading") },
                supportingText = { Text("Rendered: ${renderedHeading(item.heading, item.headingLevel)}") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Box {
                OutlinedButton(
                    onClick = { suggestionsOpen = true },
                    enabled = headingSuggestions.isNotEmpty(),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text("Suggest")
                }
                DropdownMenu(
                    expanded = suggestionsOpen,
                    onDismissRequest = { suggestionsOpen = false },
                ) {
                    headingSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion.label) },
                            onClick = {
                                item = item.copy(heading = suggestion.text, headingLevel = suggestion.level)
                                suggestionsOpen = false
                            },
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = renderedHeading(item.heading, item.headingLevel),
            onValueChange = {},
            label = { Text("Rendered heading") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true,
        )
        Text("Heading level", fontWeight = FontWeight.Bold)
        FlowRowCompat(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..6).forEach { level ->
                FilterChip(
                    selected = item.headingLevel == level,
                    onClick = { item = item.copy(headingLevel = level) },
                    label = { Text("#".repeat(level)) },
                )
            }
        }

        ToggleRow("Match case", item.matchCase) { item = item.copy(matchCase = it) }
        ToggleRow("Require same heading level", item.matchHeadingLevel) { item = item.copy(matchHeadingLevel = it) }
        ToggleRow("Add timestamp (HH:mm)", item.addTimestamp) { item = item.copy(addTimestamp = it) }
        ToggleRow("Show textbox before logging", item.showTextBox) { item = item.copy(showTextBox = it) }

        Text("Insert position", fontWeight = FontWeight.Bold)
        FlowRowCompat(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InsertPosition.entries.forEach { position ->
                FilterChip(
                    selected = item.insertPosition == position,
                    onClick = { item = item.copy(insertPosition = position) },
                    label = { Text(position.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }

        Text("Bullet type", fontWeight = FontWeight.Bold)
        FlowRowCompat(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BulletType.entries.forEach { type ->
                FilterChip(
                    selected = item.bulletType == type,
                    onClick = { item = item.copy(bulletType = type) },
                    label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }

        Button(
            onClick = { onSave(item) },
            enabled = item.heading.isNotBlank() && item.title.isNotBlank() && item.insertText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save item")
        }

        if (onDelete != null) {
            OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete item")
            }
        }
    }

    if (iconPickerOpen) {
        IconPickerDialog(
            selected = item.icon,
            onDismiss = { iconPickerOpen = false },
            onPick = {
                item = item.copy(icon = it)
                iconPickerOpen = false
            },
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowCompat(
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit,
) {
    FlowRow(
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
    ) {
        content()
    }
}

@Composable
private fun TextEntryDialog(item: QuickLogItem, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.title.ifBlank { "Add log text" }) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Append text") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = { onSubmit(text) }) {
                Text("Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun IconPickerDialog(selected: String, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose icon") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                modifier = Modifier.height(360.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(StandardIcons) { icon ->
                    Surface(
                        color = if (icon.key == selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onPick(icon.key) },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon.vector,
                                contentDescription = icon.label,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun QuickLoggerTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val darkTheme = shouldUseDarkTheme(themeMode)
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFE6D18B),
            onPrimary = Color(0xFF1C1607),
            secondary = Color(0xFFEAB676),
            surface = Color(0xFF17211D),
            background = Color(0xFF0E1512),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF1E3A34),
            onPrimary = Color.White,
            secondary = Color(0xFFB26B2B),
            surface = Color(0xFFFAF7F0),
            background = Color(0xFFFAF7F0),
        )
    }
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography.copy(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif),
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Serif),
        ),
        content = content,
    )
}

@Composable
private fun shouldUseDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

private fun appBackground(darkTheme: Boolean): Brush =
    if (darkTheme) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0E1512), Color(0xFF16231D), Color(0xFF251F13)),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFAF7F0), Color(0xFFE4EFE7), Color(0xFFF9E6BA)),
        )
    }

private val headingRegex = Regex("^(#{1,6})\\s+(.+?)\\s*#*\\s*$")

private fun renderedHeading(heading: String, fallbackLevel: Int): String {
    val match = headingRegex.matchEntire(heading.trim())
    val level = match?.groupValues?.get(1)?.length ?: fallbackLevel
    val text = match?.groupValues?.get(2)?.trim() ?: heading.trim().ifBlank { "Log" }
    return "${"#".repeat(level.coerceIn(1, 6))} $text"
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

private data class LogIcon(
    val key: String,
    val label: String,
    val vector: ImageVector,
)

private fun iconFor(key: String): LogIcon =
    StandardIcons.firstOrNull { it.key == key } ?: StandardIcons.first()

private val StandardIcons = listOf(
    LogIcon("edit", "Edit", Icons.Filled.Edit),
    LogIcon("medication", "Medication", Icons.Filled.Medication),
    LogIcon("coffee", "Coffee", Icons.Filled.LocalCafe),
    LogIcon("meal", "Meal", Icons.Filled.Restaurant),
    LogIcon("fitness", "Fitness", Icons.Filled.FitnessCenter),
    LogIcon("walk", "Walk", Icons.Filled.DirectionsWalk),
    LogIcon("sleep", "Sleep", Icons.Filled.Bedtime),
    LogIcon("sick", "Sick", Icons.Filled.Sick),
    LogIcon("healing", "Healing", Icons.Filled.Healing),
    LogIcon("mind", "Mind", Icons.Filled.Psychology),
    LogIcon("book", "Book", Icons.Filled.MenuBook),
    LogIcon("audio", "Audio", Icons.Filled.Headphones),
    LogIcon("movie", "Movie", Icons.Filled.Movie),
    LogIcon("tv", "TV", Icons.Filled.Tv),
    LogIcon("game", "Game", Icons.Filled.SportsEsports),
    LogIcon("code", "Code", Icons.Filled.Code),
    LogIcon("check", "Check", Icons.Filled.CheckCircle),
    LogIcon("warning", "Warning", Icons.Filled.Warning),
    LogIcon("fire", "Fire", Icons.Filled.Whatshot),
    LogIcon("star", "Star", Icons.Filled.Star),
    LogIcon("sun", "Sun", Icons.Filled.WbSunny),
    LogIcon("car", "Car", Icons.Filled.DirectionsCar),
    LogIcon("flight", "Flight", Icons.Filled.Flight),
    LogIcon("home", "Home", Icons.Filled.Home),
    LogIcon("clean", "Clean", Icons.Filled.CleaningServices),
    LogIcon("laundry", "Laundry", Icons.Filled.LocalLaundryService),
    LogIcon("money", "Money", Icons.Filled.Payments),
    LogIcon("receipt", "Receipt", Icons.Filled.ReceiptLong),
    LogIcon("target", "Target", Icons.Filled.AdsClick),
    LogIcon("notes", "Notes", Icons.Filled.Notes),
    LogIcon("pin", "Pin", Icons.Filled.PushPin),
    LogIcon("timer", "Timer", Icons.Filled.Timer),
    LogIcon("science", "Science", Icons.Filled.Science),
    LogIcon("heart", "Heart", Icons.Filled.Favorite),
    LogIcon("visible", "Visible", Icons.Filled.Visibility),
)

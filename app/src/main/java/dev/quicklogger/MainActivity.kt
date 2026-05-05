package dev.quicklogger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

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

@Composable
private fun UndoSnackbarHost(hostState: SnackbarHostState) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = colorScheme.surface
    val contentColor = colorScheme.onSurface
    val actionColor = colorScheme.primary
    val snackbarShape = RoundedCornerShape(8.dp)
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier.imePadding(),
    ) { snackbarData ->
        Snackbar(
            modifier = Modifier.border(
                width = 1.dp,
                color = colorScheme.outline.copy(alpha = 0.28f),
                shape = snackbarShape,
            ),
            action = snackbarData.visuals.actionLabel?.let { actionLabel ->
                {
                    TextButton(onClick = { snackbarData.performAction() }) {
                        Icon(
                            Icons.Filled.Undo,
                            contentDescription = null,
                            tint = actionColor,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(actionLabel, color = actionColor)
                    }
                }
            },
            dismissAction = if (snackbarData.visuals.withDismissAction) {
                {
                    IconButton(onClick = { snackbarData.dismiss() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = contentColor,
                        )
                    }
                }
            } else {
                null
            },
            actionOnNewLine = false,
            shape = snackbarShape,
            containerColor = containerColor,
            contentColor = contentColor,
            actionContentColor = actionColor,
            dismissActionContentColor = contentColor,
        ) {
            Text(snackbarData.visuals.message)
        }
    }
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
    var editorBackRequest by remember { mutableStateOf(0) }
    val darkTheme = shouldUseDarkTheme(config.settings.themeMode)

    suspend fun showLoggedSnackbar(item: QuickLogItem, snapshot: LogUndoSnapshot) {
        val itemTitle = item.title.ifBlank { "item" }
        snackbarHostState.currentSnackbarData?.dismiss()
        val result = snackbarHostState.showSnackbar(
            message = "Logged $itemTitle",
            actionLabel = "Undo",
            duration = SnackbarDuration.Long,
            withDismissAction = true,
        )
        if (result == SnackbarResult.ActionPerformed) {
            runCatching { repository.undoLog(snapshot) }
                .onSuccess { snackbarHostState.showSnackbar("Undid $itemTitle", duration = SnackbarDuration.Short) }
                .onFailure { snackbarHostState.showSnackbar(it.message ?: "Could not undo log item.") }
        }
    }

    SideEffect {
        activity?.window?.let { window ->
            window.statusBarColor = AndroidColor.TRANSPARENT
            window.navigationBarColor = AndroidColor.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    BackHandler(screen !is Screen.Home) {
        if (screen is Screen.EditItem) {
            editorBackRequest++
        } else {
            screen = Screen.Home
        }
    }

    Scaffold(
        snackbarHost = { UndoSnackbarHost(snackbarHostState) },
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
                        IconButton(onClick = {
                            if (screen is Screen.EditItem) {
                                editorBackRequest++
                            } else {
                                screen = Screen.Home
                            }
                        }) {
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
                                    .onSuccess { snapshot -> showLoggedSnackbar(item, snapshot) }
                                    .onFailure { snackbarHostState.showSnackbar(it.message ?: "Could not write log item.") }
                            }
                        }
                    },
                    onItemEdit = { screen = Screen.EditItem(it) },
                    onReorder = { reordered ->
                        scope.launch { repository.reorderItems(reordered) }
                    },
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
                    backRequest = editorBackRequest,
                    onDone = { item ->
                        scope.launch {
                            if (item != null) repository.saveItem(item)
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
                        .onSuccess { snapshot -> showLoggedSnackbar(item, snapshot) }
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
    onItemEdit: (QuickLogItem) -> Unit,
    onReorder: (List<QuickLogItem>) -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    var orderedItems by remember(config.items) { mutableStateOf(config.items) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val gridState = rememberLazyGridState()
    val hapticFeedback = LocalHapticFeedback.current
    val setupVisible = config.settings.logRootUri == null || config.settings.templateUri == null
    val filteredItems = remember(orderedItems, searchQuery) {
        orderedItems.filterBySearch(searchQuery)
    }
    val isFiltering = searchQuery.isNotBlank()
    val reorderableGridState = rememberReorderableLazyGridState(gridState) { from, to ->
        if (isFiltering) return@rememberReorderableLazyGridState
        orderedItems = orderedItems.toMutableList().also { list ->
            list.add(to.index, list.removeAt(from.index))
        }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
    ) {
        if (setupVisible) {
            SetupCard(onSettingsClick)
        }
        if (config.items.isEmpty()) {
            EmptyState(onAddClick)
        } else {
            SearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 6.dp),
            )
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = gridState,
                columns = GridCells.Adaptive(156.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(filteredItems, key = { _, item -> item.id }) { _, item ->
                    ReorderableItem(reorderableGridState, key = item.id) { isDragging ->
                        val dragModifier = if (isFiltering) {
                            Modifier
                        } else {
                            Modifier.longPressDraggableHandle(
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                    onReorder(orderedItems)
                                },
                            )
                        }
                        QuickLogTile(
                            item = item,
                            dragging = isDragging,
                            onClick = { onItemClick(item) },
                            onEditClick = { onItemEdit(item) },
                            modifier = dragModifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text("Filter items") },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            if (value.isNotBlank()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear filter")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(22.dp),
    )
}

private fun List<QuickLogItem>.filterBySearch(query: String): List<QuickLogItem> {
    val terms = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (terms.isEmpty()) return this
    return filter { item ->
        val haystack = listOf(item.title, item.insertText, item.heading, item.icon)
            .joinToString(" ")
            .lowercase()
        terms.all { term -> term.lowercase() in haystack }
    }
}

@Composable
private fun SetupCard(onSettingsClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
private fun QuickLogTile(
    item: QuickLogItem,
    dragging: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (dragging) 6.dp else 1.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .height(112.dp)
            .combinedClickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = iconFor(item.icon).vector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(34.dp),
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Edit item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(end = 18.dp),
            ) {
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

@Composable
private fun ItemEditorScreen(
    initial: QuickLogItem?,
    repository: ConfigRepository,
    backRequest: Int,
    onDone: (QuickLogItem?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val startingItem = remember(initial?.id) { initial ?: QuickLogItem() }
    var item by remember(initial?.id) { mutableStateOf(startingItem) }
    var iconPickerOpen by remember { mutableStateOf(false) }
    var headingSuggestions by remember { mutableStateOf(emptyList<HeadingSuggestion>()) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var handledBackRequest by remember(initial?.id) { mutableStateOf(backRequest) }

    fun isValid(candidate: QuickLogItem): Boolean =
        candidate.heading.isNotBlank() &&
            candidate.title.isNotBlank() &&
            (candidate.insertText.isNotBlank() || candidate.showTextBox)

    fun hasMeaningfulChanges(candidate: QuickLogItem): Boolean =
        startingItem != candidate

    fun leaveEditor() {
        when {
            isValid(item) -> onDone(item)
            hasMeaningfulChanges(item) -> showDiscardDialog = true
            else -> onDone(null)
        }
    }

    BackHandler { leaveEditor() }

    LaunchedEffect(backRequest) {
        if (backRequest != handledBackRequest) {
            handledBackRequest = backRequest
            leaveEditor()
        }
    }

    LaunchedEffect(initial?.id) {
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
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        HeadingComboBox(
            value = item.heading,
            suggestions = headingSuggestions,
            onValueChange = { item = item.copy(heading = it) },
            onSuggestion = { suggestion ->
                item = item.copy(heading = suggestion.label)
            },
            modifier = Modifier.fillMaxWidth(),
        )

        ToggleRow("Match case", item.matchCase) { item = item.copy(matchCase = it) }
        ToggleRow("Show textbox before logging", item.showTextBox) { item = item.copy(showTextBox = it) }

        Text("Timestamp", fontWeight = FontWeight.Bold)
        FlowRowCompat(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimestampMode.entries.forEach { mode ->
                FilterChip(
                    selected = item.timestampMode == mode,
                    onClick = { item = item.copy(timestampMode = mode) },
                    label = {
                        Text(
                            when (mode) {
                                TimestampMode.None -> "None"
                                TimestampMode.Minutes -> "HH:mm"
                                TimestampMode.Seconds -> "HH:mm:ss"
                            },
                        )
                    },
                )
            }
        }

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

        Text(
            if (isValid(item)) "Back saves this item." else "Complete required fields, or go back to discard.",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
        )

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

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard incomplete item?") },
            text = { Text("This item is not complete enough to create or update.") },
            confirmButton = {
                Button(onClick = { onDone(null) }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep editing")
                }
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

@Composable
private fun SmallDropdown(
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(value)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun HeadingComboBox(
    value: String,
    suggestions: List<HeadingSuggestion>,
    onValueChange: (String) -> Unit,
    onSuggestion: (HeadingSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var showAllSuggestions by remember { mutableStateOf(false) }
    val filtered = if (showAllSuggestions) {
        suggestions
    } else {
        val query = value.trimStart('#').trim()
        suggestions.filter {
            query.isBlank() || it.text.contains(query, ignoreCase = true)
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                showAllSuggestions = false
                expanded = true
            },
            label = { Text("Heading") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        showAllSuggestions = true
                        expanded = true
                    },
                ) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Show headings")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (filtered.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No headings found") },
                    enabled = false,
                    onClick = {},
                )
            }
            filtered.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion.label) },
                    onClick = {
                        onSuggestion(suggestion)
                        expanded = false
                    },
                )
            }
        }
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
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(120)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.title.ifBlank { "Add log text" }) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Append text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
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
            primary = Color(0xFFF4F4F5),
            onPrimary = Color(0xFF18181B),
            secondary = Color(0xFFB4B4BC),
            surface = Color(0xFF303034),
            onSurface = Color(0xFFF4F4F5),
            background = Color(0xFF0B0B0D),
            onBackground = Color(0xFFF4F4F5),
            inversePrimary = Color(0xFF18181B),
            inverseSurface = Color(0xFFF4F4F5),
            inverseOnSurface = Color(0xFF18181B),
            outline = Color(0xFF6F6F78),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF18181B),
            onPrimary = Color.White,
            secondary = Color(0xFF52525B),
            surface = Color.White,
            onSurface = Color(0xFF18181B),
            background = Color(0xFFE9EAEC),
            onBackground = Color(0xFF18181B),
            inversePrimary = Color(0xFFF4F4F5),
            inverseSurface = Color(0xFF18181B),
            inverseOnSurface = Color(0xFFF4F4F5),
            outline = Color(0xFFB8BAC0),
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
            colors = listOf(Color(0xFF0B0B0D), Color(0xFF161619)),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF1F2F4), Color(0xFFE2E3E6)),
        )
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

package dev.quicklogger

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

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
    val activity = context as? ComponentActivity
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var textItem by remember { mutableStateOf<QuickLogItem?>(null) }
    var editorBackRequest by remember { mutableStateOf(0) }
    val darkTheme = shouldUseDarkTheme(config.settings.themeMode)

    fun leaveCurrentScreen() {
        if (screen is Screen.EditItem) {
            editorBackRequest++
        } else {
            screen = Screen.Home
        }
    }

    fun logItem(item: QuickLogItem, text: String = "") {
        scope.launch {
            logItemWithUndoSnackbar(repository, snackbarHostState, item, text)
        }
    }

    SideEffect {
        activity?.enableEdgeToEdge(
            statusBarStyle = if (darkTheme) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            },
            navigationBarStyle = if (darkTheme) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            },
        )
    }

    BackHandler(screen !is Screen.Home) { leaveCurrentScreen() }

    Scaffold(
        snackbarHost = { UndoSnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
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
                        IconButton(onClick = { leaveCurrentScreen() }) {
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
                            logItem(item)
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
                logItem(item, text)
            },
        )
    }
}

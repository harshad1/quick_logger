package dev.quicklogger

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

suspend fun logItemWithUndoSnackbar(
    repository: ConfigRepository,
    snackbarHostState: SnackbarHostState,
    item: QuickLogItem,
    text: String = "",
) {
    runCatching { repository.logItem(item, text) }
        .onSuccess { snapshot -> showLoggedSnackbar(repository, snackbarHostState, item, snapshot) }
        .onFailure { snackbarHostState.showSnackbar(it.message ?: "Could not write log item.") }
}

private suspend fun showLoggedSnackbar(
    repository: ConfigRepository,
    snackbarHostState: SnackbarHostState,
    item: QuickLogItem,
    snapshot: LogUndoSnapshot,
) {
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

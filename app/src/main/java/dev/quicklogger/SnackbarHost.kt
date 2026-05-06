package dev.quicklogger

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UndoSnackbarHost(hostState: SnackbarHostState) {
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
                            Icons.AutoMirrored.Filled.Undo,
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

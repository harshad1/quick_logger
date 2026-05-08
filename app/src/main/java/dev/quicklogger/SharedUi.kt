package dev.quicklogger

import android.view.WindowManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SmallDropdown(
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
fun HeadingComboBox(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlowRowCompat(
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
fun TextEntryDialog(item: QuickLogItem, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.title.ifBlank { "Add log text" }) },
        text = {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            DisposableEffect(dialogWindow) {
                val previousSoftInputMode = dialogWindow?.attributes?.softInputMode
                dialogWindow?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                onDispose {
                    previousSoftInputMode?.let { dialogWindow.setSoftInputMode(it) }
                }
            }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
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
fun IconPickerDialog(selected: String, onDismiss: () -> Unit, onPick: (String) -> Unit) {
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

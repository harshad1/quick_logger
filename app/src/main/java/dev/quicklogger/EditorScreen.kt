package dev.quicklogger

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ItemEditorScreen(
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

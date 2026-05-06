/*
 * Copyright 2026 Maximilian Schwärzler
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.co.schwaerzler.maximilian.doit.ui.navigation.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.co.schwaerzler.maximilian.doit.R
import at.co.schwaerzler.maximilian.doit.data.EditTodoViewModel
import at.co.schwaerzler.maximilian.doit.data.EditTodoViewModel.EditTodoUiState
import at.co.schwaerzler.maximilian.doit.ui.component.MaxWidthLayout
import at.co.schwaerzler.maximilian.doit.ui.theme.DoItTheme
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant

@Composable
fun EditTodoScreen(
    todoId: Int?,
    navigateBack: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditTodoViewModel = viewModel(factory = EditTodoViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isModified by viewModel.isModified.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(todoId) {
        if (todoId != null) {
            viewModel.loadTodo(todoId)
        }
    }

    BackHandler(enabled = isModified) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved changes will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onCancel()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") }
            }
        )
    }

    EditTodoScreenContent(
        id = todoId,
        uiState = uiState,
        onTitleChange = { viewModel.updateTitle(it) },
        onDescriptionChange = { viewModel.updateDescription(it) },
        onDeadlineChange = { viewModel.updateDeadline(it) },
        onSave = { if (viewModel.saveTodo(todoId)) navigateBack() },
        onCancel = {
            if (isModified) {
                showDiscardDialog = true
            } else {
                onCancel()
            }
        },
        onDelete = {
            viewModel.deleteTodo()
            navigateBack()
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTodoScreenContent(
    id: Int?,
    uiState: EditTodoUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDeadlineChange: (Instant?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateMillis by remember { mutableLongStateOf(0L) }
    val activity = LocalActivity.current

    fun onShareTodo() {
        val shareContentBuilder = StringBuilder()
        shareContentBuilder.append("*")
        shareContentBuilder.append(uiState.title)
        shareContentBuilder.append("*")
        shareContentBuilder.append("\n")
        if (uiState.description.isNotBlank()) {
            shareContentBuilder.append(uiState.description)
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareContentBuilder.toString())
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        activity?.startActivity(shareIntent)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.deadline?.toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = datePickerState.selectedDateMillis
                    if (selected != null) {
                        pendingDateMillis = selected
                        showDatePicker = false
                        showTimePicker = true
                    }
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val (initHour, initMinute) = uiState.deadline
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.let { it.hour to it.minute }
            ?: (12 to 0)
        DeadlineTimePickerDialog(
            initialHour = initHour,
            initialMinute = initMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                onDeadlineChange(buildInstant(pendingDateMillis, hour, minute))
                showTimePicker = false
            }
        )
    }

    Scaffold(
        modifier
            .fillMaxSize()
            .imePadding(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSave,
                icon = {
                    Icon(painterResource(R.drawable.add_task_24px), contentDescription = null)
                },
                text = {
                    Text("Done")
                }
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    if (id == null) {
                        Text("Add new TODO")
                    } else {
                        Text("Edit TODO")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onCancel() }) {
                        Icon(painterResource(R.drawable.arrow_back_24px), contentDescription = null)
                    }
                },
                actions = {
                    if (id != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                painterResource(R.drawable.delete_24px),
                                contentDescription = "Delete TODO"
                            )
                        }

                        IconButton(onClick = { onShareTodo() }) {
                            Icon(
                                painterResource(R.drawable.share_24px),
                                contentDescription = "Share TODO"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        MaxWidthLayout(Modifier.padding(innerPadding)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                OutlinedTextField(
                    uiState.title,
                    onValueChange = onTitleChange,
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    label = {
                        Text("Title")
                    },
                    supportingText = {
                        Text(uiState.titleError ?: "Required")
                    },
                    isError = uiState.titleError != null,
                    leadingIcon = {
                        Icon(painterResource(R.drawable.title_24px), contentDescription = null)
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                OutlinedTextField(
                    uiState.description,
                    onValueChange = onDescriptionChange,
                    Modifier.fillMaxWidth(),
                    label = {
                        Text("Description")
                    },
                    minLines = 5,
                    supportingText = {
                        Text(uiState.descriptionError ?: "Optional")
                    },
                    isError = uiState.descriptionError != null
                )

                val deadlineSet = uiState.deadline != null

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = deadlineSet,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showDatePicker = true
                            } else {
                                onDeadlineChange(null)
                            }
                        }
                    )
                    Text("Set deadline")
                }

                if (uiState.deadline != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.deadline.formatDeadline(),
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            label = { Text("Deadline") },
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.event_24px),
                                    contentDescription = null
                                )
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { showDatePicker = true }
                                )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlineTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select time") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun buildInstant(dateEpochMs: Long, hour: Int, minute: Int): Instant {
    // DatePicker returns UTC midnight; resolve local date first to handle DST correctly
    val localDate = Instant.fromEpochMilliseconds(dateEpochMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    return LocalDateTime(localDate, LocalTime(hour, minute))
        .toInstant(TimeZone.currentSystemDefault())
}

private fun Instant.formatDeadline(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(local)
}

@Preview(showBackground = true)
@Composable
private fun EditTodoScreenAddPreview() {
    DoItTheme {
        EditTodoScreenContent(
            id = null,
            uiState = EditTodoUiState(),
            onTitleChange = {},
            onDescriptionChange = {},
            onDeadlineChange = {},
            onSave = {},
            onCancel = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTodoScreenEditPreview() {
    DoItTheme {
        EditTodoScreenContent(
            id = 1,
            uiState = EditTodoUiState(
                title = "Buy groceries",
                description = "Milk, eggs, bread",
            ),
            onTitleChange = {},
            onDescriptionChange = {},
            onDeadlineChange = {},
            onSave = {},
            onCancel = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTodoScreenValidationErrorPreview() {
    DoItTheme {
        EditTodoScreenContent(
            id = null,
            uiState = EditTodoUiState(
                title = "",
                titleError = "Title is required",
            ),
            onTitleChange = {},
            onDescriptionChange = {},
            onDeadlineChange = {},
            onSave = {},
            onCancel = {},
            onDelete = {}
        )
    }
}

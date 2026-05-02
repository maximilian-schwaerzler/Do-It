package at.co.schwaerzler.maximilian.doit.ui.navigation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import at.co.schwaerzler.maximilian.doit.ui.theme.DoItTheme

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
        onSave = { if (viewModel.saveTodo(todoId)) navigateBack() },
        onCancel = {
            if (isModified) {
                showDiscardDialog = true
            } else {
                onCancel()
            }
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
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
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
        }
    }
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
            onSave = {},
            onCancel = {}
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
            onSave = {},
            onCancel = {}
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
            onSave = {},
            onCancel = {}
        )
    }
}
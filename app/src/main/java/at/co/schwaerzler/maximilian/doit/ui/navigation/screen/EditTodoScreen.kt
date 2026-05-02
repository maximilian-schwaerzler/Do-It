package at.co.schwaerzler.maximilian.doit.ui.navigation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    modifier: Modifier = Modifier,
    viewModel: EditTodoViewModel = viewModel(factory = EditTodoViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EditTodoScreenContent(
        uiState = uiState,
        onTitleChange = { viewModel.updateTitle(it) },
        onDescriptionChange = { viewModel.updateDescription(it) },
        onSave = { if (viewModel.saveTodo(todoId)) navigateBack() },
        modifier = modifier,
    )
}

@Composable
private fun EditTodoScreenContent(
    uiState: EditTodoUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
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
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
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
            uiState = EditTodoUiState(),
            onTitleChange = {},
            onDescriptionChange = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTodoScreenEditPreview() {
    DoItTheme {
        EditTodoScreenContent(
            uiState = EditTodoUiState(
                title = "Buy groceries",
                description = "Milk, eggs, bread",
            ),
            onTitleChange = {},
            onDescriptionChange = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTodoScreenValidationErrorPreview() {
    DoItTheme {
        EditTodoScreenContent(
            uiState = EditTodoUiState(
                title = "",
                titleError = "Title is required",
            ),
            onTitleChange = {},
            onDescriptionChange = {},
            onSave = {},
        )
    }
}
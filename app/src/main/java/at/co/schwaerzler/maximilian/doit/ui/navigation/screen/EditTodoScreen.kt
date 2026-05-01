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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.co.schwaerzler.maximilian.doit.R
import at.co.schwaerzler.maximilian.doit.data.EditTodoViewModel

@Composable
fun EditTodoScreen(
    todoId: Int?,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditTodoViewModel = viewModel(factory = EditTodoViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier
            .fillMaxSize()
            .imePadding(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (viewModel.saveTodo(todoId)) {
                        navigateBack()
                    }
                },
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
                onValueChange = { viewModel.updateTitle(it) },
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
                onValueChange = { viewModel.updateDescription(it) },
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
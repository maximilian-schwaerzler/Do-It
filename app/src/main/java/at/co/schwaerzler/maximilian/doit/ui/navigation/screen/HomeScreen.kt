package at.co.schwaerzler.maximilian.doit.ui.navigation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.co.schwaerzler.maximilian.doit.R
import at.co.schwaerzler.maximilian.doit.data.HomeViewModel
import at.co.schwaerzler.maximilian.doit.data.db.entity.Todo
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.ui.components.TodoListItem
import at.co.schwaerzler.maximilian.doit.ui.theme.DoItTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTodo: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val openTodos by viewModel.openTodos.collectAsStateWithLifecycle(emptyList())
    val doneTodos by viewModel.doneTodos.collectAsStateWithLifecycle(emptyList())

    HomeScreenContent(
        openTodos = openTodos,
        doneTodos = doneTodos,
        onAddTodo = onAddTodo,
        onStateToggle = { viewModel.toggleTodoDone(it) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    openTodos: List<Todo>,
    doneTodos: List<Todo>,
    onAddTodo: () -> Unit,
    onStateToggle: (Todo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddTodo() },
                text = {
                    Text("Add a new TODO")
                },
                icon = {
                    Icon(painterResource(R.drawable.add_24px), contentDescription = "Add new todo")
                })
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (openTodos.isNotEmpty()) {
                item(key = "open-headline") {
                    ListItem(
                        headlineContent = {
                            Text("Open", style = MaterialTheme.typography.headlineSmall)
                        },
                        modifier = Modifier.animateItem()
                    )
                    HorizontalDivider()
                }
            }
            items(openTodos, key = { it.id }) { item ->
                TodoListItem(
                    item, onStateToggle = { onStateToggle(item) },
                    Modifier.animateItem()
                )
            }
            if (doneTodos.isNotEmpty()) {
                item(key = "done-headline") {
                    if (openTodos.isNotEmpty()) {
                        HorizontalDivider()
                    }
                    ListItem(
                        headlineContent = {
                            Text("Done", style = MaterialTheme.typography.headlineSmall)
                        },
                        Modifier.animateItem()
                    )
                    HorizontalDivider()
                }
            }
            items(doneTodos, key = { it.id }) { item ->
                TodoListItem(
                    item, onStateToggle = { onStateToggle(item) },
                    Modifier.animateItem()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    DoItTheme {
        HomeScreenContent(
            openTodos = emptyList(),
            doneTodos = emptyList(),
            onAddTodo = {},
            onStateToggle = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenWithTodosPreview() {
    DoItTheme {
        HomeScreenContent(
            openTodos = listOf(
                Todo(id = 1, title = "Buy groceries", description = "Milk, eggs, bread", deadlineDateTime = null),
                Todo(id = 2, title = "Read a book", description = null, deadlineDateTime = null),
            ),
            doneTodos = listOf(
                Todo(id = 3, title = "Fix the bug", description = "The login crash", deadlineDateTime = null, state = TodoState.DONE),
            ),
            onAddTodo = {},
            onStateToggle = {},
        )
    }
}
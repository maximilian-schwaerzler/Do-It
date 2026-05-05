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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.co.schwaerzler.maximilian.doit.R
import at.co.schwaerzler.maximilian.doit.data.HomeViewModel
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import at.co.schwaerzler.maximilian.doit.ui.component.MaxWidthLayout
import at.co.schwaerzler.maximilian.doit.ui.component.TodoListItem
import at.co.schwaerzler.maximilian.doit.ui.theme.DoItTheme
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTodo: () -> Unit,
    onClickTodo: (id: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val todos by viewModel.todos.collectAsStateWithLifecycle(Pair(emptyList(), emptyList()))
    val (openTodos, doneTodos) = todos

    var selectedTodos by rememberSaveable {
        mutableStateOf<Set<Int>>(emptySet())
    }

    BackHandler(enabled = selectedTodos.isNotEmpty()) {
        selectedTodos = emptySet()
    }

    HomeScreenContent(
        openTodos = openTodos,
        doneTodos = doneTodos,
        onAddTodo = onAddTodo,
        onClickTodo = onClickTodo,
        toggleTodoItemSelection = { id ->
            selectedTodos = if (id in selectedTodos) selectedTodos - id else selectedTodos + id
        },
        onStateToggle = { viewModel.toggleTodoDone(it) },
        selectedTodos = selectedTodos,
        onClearSelection = {
            selectedTodos = emptySet()
        },
        onDeleteSelection = {
            viewModel.deleteTodosByIds(selectedTodos.toList())
            selectedTodos = emptySet()
        },
        modifier = modifier,
        onSelectAll = {
            selectedTodos += openTodos.map { it.id }
            selectedTodos += doneTodos.map { it.id }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    openTodos: List<TodoSummary>,
    doneTodos: List<TodoSummary>,
    onAddTodo: () -> Unit,
    onClickTodo: (id: Int) -> Unit,
    toggleTodoItemSelection: (id: Int) -> Unit,
    onStateToggle: (TodoSummary) -> Unit,
    selectedTodos: Set<Int>,
    onClearSelection: () -> Unit,
    onDeleteSelection: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectAll: () -> Unit,
) {
    val selectionToolbar = selectedTodos.isNotEmpty()

    Scaffold(
        modifier.fillMaxSize(), topBar = {
            Crossfade(
                targetState = selectionToolbar,
                label = "topBar"
            ) { inSelectionMode ->
                if (inSelectionMode) {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(
                                    R.string.todos_selected_template,
                                    selectedTodos.size
                                ))
                        },
                        navigationIcon = {
                            IconButton(onClick = onClearSelection) {
                                Icon(
                                    painterResource(R.drawable.close_24px),
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onDeleteSelection) {
                                Icon(
                                    painterResource(R.drawable.delete_24px),
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = onSelectAll) {
                                Icon(
                                    painterResource(R.drawable.select_all_24px),
                                    contentDescription = null
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(stringResource(R.string.just_do_it_app_bar))
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTodo,
                text = {
                    Text(stringResource(R.string.add_new_todo_fab))
                },
                icon = {
                    Icon(painterResource(R.drawable.add_24px), contentDescription = stringResource(R.string.add_new_todo_fab))
                })
        }
    ) { innerPadding ->
        MaxWidthLayout(Modifier.padding(innerPadding)) {
            if (openTodos.isNotEmpty() || doneTodos.isNotEmpty()) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                ) {
                    if (openTodos.isNotEmpty()) {
                        item(key = "open-headline") {
                            ListItem(
                                headlineContent = {
                                    Text(stringResource(R.string.open_headline), style = MaterialTheme.typography.headlineSmall)
                                },
                                modifier = Modifier.animateItem()
                            )
                            HorizontalDivider()
                        }
                    } else {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 70.dp)
                                    .animateItem(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.you_did_everything_empty_text),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }
                    items(openTodos, key = { it.id }) { item ->
                        TodoListItem(
                            item,
                            onStateToggle = { onStateToggle(item) },
                            onClick = {
                                if (selectionToolbar) {
                                    toggleTodoItemSelection(item.id)
                                } else {
                                    onClickTodo(item.id)
                                }
                            },
                            onLongClick = {
                                toggleTodoItemSelection(item.id)
                            },
                            selected = selectedTodos.contains(item.id),
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
                                    Text(stringResource(R.string.done_headline), style = MaterialTheme.typography.headlineSmall)
                                },
                                Modifier.animateItem()
                            )
                            HorizontalDivider()
                        }
                    }
                    items(doneTodos, key = { it.id }) { item ->
                        TodoListItem(
                            item,
                            onStateToggle = { onStateToggle(item) },
                            onClick = {
                                if (selectionToolbar) {
                                    toggleTodoItemSelection(item.id)
                                } else {
                                    onClickTodo(item.id)
                                }
                            },
                            onLongClick = {
                                toggleTodoItemSelection(item.id)
                            },
                            selected = selectedTodos.contains(item.id),
                            Modifier.animateItem()
                        )
                    }
                }
            } else {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.nothing_to_do_empty_text),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(stringResource(R.string.add_new_todo_button))
                    }
                }
            }
        }
    }
}

private val previewTodos = listOf(
    TodoSummary(1, "Buy groceries", null, TodoState.OPEN, Clock.System.now()),
    TodoSummary(2, "Read a book", null, TodoState.OPEN, Clock.System.now()),
    TodoSummary(3, "Fix the bug", null, TodoState.DONE, Clock.System.now()),
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    DoItTheme {
        HomeScreenContent(
            openTodos = emptyList(),
            doneTodos = emptyList(),
            onAddTodo = {},
            onClickTodo = {},
            toggleTodoItemSelection = {},
            onStateToggle = {},
            selectedTodos = emptySet(),
            onClearSelection = {},
            onSelectAll = {},
            onDeleteSelection = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenWithTodosPreview() {
    DoItTheme {
        HomeScreenContent(
            openTodos = previewTodos.filter { it.state == TodoState.OPEN },
            doneTodos = previewTodos.filter { it.state == TodoState.DONE },
            onAddTodo = {},
            onClickTodo = {},
            toggleTodoItemSelection = {},
            onStateToggle = {},
            selectedTodos = emptySet(),
            onClearSelection = {},
            onSelectAll = {},
            onDeleteSelection = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenAllDonePreview() {
    DoItTheme {
        HomeScreenContent(
            openTodos = emptyList(),
            doneTodos = previewTodos.filter { it.state == TodoState.DONE },
            onAddTodo = {},
            onClickTodo = {},
            toggleTodoItemSelection = {},
            onStateToggle = {},
            selectedTodos = emptySet(),
            onClearSelection = {},
            onSelectAll = {},
            onDeleteSelection = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenSelectionPreview() {
    DoItTheme {
        HomeScreenContent(
            openTodos = previewTodos.filter { it.state == TodoState.OPEN },
            doneTodos = previewTodos.filter { it.state == TodoState.DONE },
            onAddTodo = {},
            onClickTodo = {},
            toggleTodoItemSelection = {},
            onStateToggle = {},
            selectedTodos = setOf(1),
            onClearSelection = {},
            onSelectAll = {},
            onDeleteSelection = {}
        )
    }
}
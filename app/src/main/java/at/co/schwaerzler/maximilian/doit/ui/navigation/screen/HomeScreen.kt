package at.co.schwaerzler.maximilian.doit.ui.navigation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import at.co.schwaerzler.maximilian.doit.HomeViewModel
import at.co.schwaerzler.maximilian.doit.R
import at.co.schwaerzler.maximilian.doit.ui.components.TodoListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTodo: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val todos by viewModel.todos.collectAsStateWithLifecycle(emptyList())

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
            items(todos, key = { it.id }) { item ->
                TodoListItem(item)
            }
        }
    }
}
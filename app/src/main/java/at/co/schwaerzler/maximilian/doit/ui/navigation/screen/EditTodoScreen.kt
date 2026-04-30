package at.co.schwaerzler.maximilian.doit.ui.navigation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EditTodoScreen(todoId: String?, modifier: Modifier = Modifier) {
    Scaffold(modifier.fillMaxSize()) { innerPadding ->
        Column(Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            Text("EditTodoScreen")
            Text("ID: $todoId")
        }
    }
}
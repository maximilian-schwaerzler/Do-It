package at.co.schwaerzler.maximilian.doit.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.co.schwaerzler.maximilian.doit.DoItApplication
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase
import at.co.schwaerzler.maximilian.doit.data.db.entity.Todo
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import kotlinx.coroutines.launch

class HomeViewModel(
    private val db: TodoDatabase
) : ViewModel() {
    val openTodos = db.todoDao().getOpen()
    val doneTodos = db.todoDao().getDone()

    fun toggleTodoDone(todo: Todo) {
        viewModelScope.launch {
            if (todo.state == TodoState.OPEN) {
                db.todoDao().update(
                    todo.copy(
                        state = TodoState.DONE
                    )
                )
            } else if (todo.state == TodoState.DONE) {
                db.todoDao().update(
                    todo.copy(
                        state = TodoState.OPEN
                    )
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db =
                    (this[APPLICATION_KEY] as DoItApplication).database
                HomeViewModel(
                    db = db
                )
            }
        }
    }
}
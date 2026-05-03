package at.co.schwaerzler.maximilian.doit.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.co.schwaerzler.maximilian.doit.DoItApplication
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val db: TodoDatabase
) : ViewModel() {
    val todos = combine(
        db.todoDao().getOpenSummaries(),
        db.todoDao().getDoneSummaries()
    ) { open, done -> Pair(open, done) }

    fun toggleTodoDone(todo: TodoSummary) {
        viewModelScope.launch {
            val newState = if (todo.state == TodoState.OPEN) TodoState.DONE else TodoState.OPEN
            db.todoDao().updateState(todo.id, newState)
        }
    }

    fun deleteTodosByIds(ids: List<Int>) {
        viewModelScope.launch {
            db.todoDao().deleteByIds(ids)
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
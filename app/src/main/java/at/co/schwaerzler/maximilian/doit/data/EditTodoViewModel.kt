package at.co.schwaerzler.maximilian.doit.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.co.schwaerzler.maximilian.doit.DoItApplication
import at.co.schwaerzler.maximilian.doit.R
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase
import at.co.schwaerzler.maximilian.doit.data.db.entity.Todo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

class EditTodoViewModel(
    private val appContext: Context,
    private val db: TodoDatabase
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditTodoUiState>(EditTodoUiState())
    val uiState = _uiState.asStateFlow()

    private var originalTodo: Todo? = null
    private var originalTitle = ""
    private var originalDescription = ""
    private var originalDeadline: Instant? = null

    val isModified = uiState.map { state ->
        state.title != originalTitle ||
                state.description != originalDescription ||
                state.deadline != originalDeadline
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun updateTitle(newTitle: String) {
        _uiState.update {
            it.copy(
                title = newTitle
            )
        }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update {
            it.copy(
                description = newDescription
            )
        }
    }

    fun updateDeadline(newDeadline: Instant?) {
        _uiState.update {
            it.copy(
                deadline = newDeadline
            )
        }
    }

    fun saveTodo(id: Int?): Boolean {
        if (_uiState.value.title.isBlank()) {
            _uiState.update { it.copy(titleError = appContext.getString(R.string.title_is_required)) }
            return false
        }

        if (id == null) {
            viewModelScope.launch {
                db.todoDao().insert(
                    Todo(
                        id = 0,
                        uiState.value.title,
                        uiState.value.description.ifBlank { null },
                        deadlineDateTime = uiState.value.deadline
                    )
                )
            }
            return true
        } else {
            val original = originalTodo ?: return false
            viewModelScope.launch {
                db.todoDao().update(
                    original.copy(
                        title = uiState.value.title,
                        description = uiState.value.description.ifBlank { null },
                        deadlineDateTime = uiState.value.deadline
                    )
                )
            }
            return true
        }
    }

    fun deleteTodo() {
        val todo = originalTodo ?: return
        viewModelScope.launch {
            db.todoDao().delete(todo)
        }
    }

    fun loadTodo(id: Int) {
        viewModelScope.launch {
            val todo = db.todoDao().getById(id) ?: return@launch
            originalTodo = todo
            originalTitle = todo.title
            originalDescription = todo.description ?: ""
            originalDeadline = todo.deadlineDateTime
            _uiState.update {
                it.copy(
                    title = todo.title,
                    description = todo.description ?: "",
                    deadline = todo.deadlineDateTime
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db =
                    (this[APPLICATION_KEY] as DoItApplication).database
                val appContext = (this[APPLICATION_KEY] as DoItApplication).applicationContext
                EditTodoViewModel(
                    appContext = appContext,
                    db = db
                )
            }
        }
    }

    data class EditTodoUiState(
        val title: String = "",
        val description: String = "",
        val titleError: String? = null,
        val descriptionError: String? = null,
        val deadline: Instant? = null
    )
}
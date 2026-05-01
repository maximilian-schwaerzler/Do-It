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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

class EditTodoViewModel(
    private val db: TodoDatabase
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditTodoUiState>(EditTodoUiState())
    val uiState = _uiState.asStateFlow()

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
            _uiState.update { it.copy(titleError = "Title is required") }
            return false
        }

        if (id == null || id == 0) {
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
            // TODO
            throw NotImplementedError()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db =
                    (this[APPLICATION_KEY] as DoItApplication).database
                EditTodoViewModel(
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
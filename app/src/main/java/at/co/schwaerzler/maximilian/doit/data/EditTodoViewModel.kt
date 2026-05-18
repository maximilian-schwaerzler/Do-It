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

package at.co.schwaerzler.maximilian.doit.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.co.schwaerzler.maximilian.doit.DoItApplication
import androidx.glance.appwidget.updateAll
import at.co.schwaerzler.maximilian.doit.OverviewWidget
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
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

/**
 * ViewModel for the add/edit screen.
 *
 * Handles both creating a new todo (`id == null`) and editing an existing one (`id != null`).
 * The original field values are stored on [loadTodo] so that [isModified] can detect unsaved changes.
 */
class EditTodoViewModel(
    private val appContext: Context,
    private val db: TodoDatabase
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditTodoUiState>(EditTodoUiState())

    /** Current form state exposed to the UI. */
    val uiState = _uiState.asStateFlow()

    private var originalTodo: Todo? = null
    private var originalTitle = ""
    private var originalDescription = ""
    private var originalDeadline: Instant? = null

    /**
     * `true` when any form field differs from the values loaded by [loadTodo].
     *
     * Always `false` for a new todo (nothing has been persisted yet).
     */
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

    /**
     * Persists the current form state.
     *
     * Inserts a new todo when [id] is `null`, or updates the existing one otherwise.
     *
     * @param id Primary key of the todo to update, or `null` to create a new one.
     * @return `true` on success, `false` if validation failed or the original todo could not be found.
     */
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
                withContext(NonCancellable) { OverviewWidget().updateAll(appContext) }
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
                withContext(NonCancellable) { OverviewWidget().updateAll(appContext) }
            }
            return true
        }
    }

    /** Deletes the todo that was loaded via [loadTodo]. No-op if no todo has been loaded. */
    fun deleteTodo() {
        val todo = originalTodo ?: return
        viewModelScope.launch {
            db.todoDao().delete(todo)
            withContext(NonCancellable) { OverviewWidget().updateAll(appContext) }
        }
    }

    /**
     * Loads the todo with the given [id] from the database and populates the form state.
     *
     * Also stores the original values so [isModified] can compare against them.
     */
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

    /**
     * Immutable snapshot of the add/edit form.
     *
     * @property titleError Non-null when the title field has a validation error to display.
     * @property descriptionError Non-null when the description field has a validation error to display.
     */
    data class EditTodoUiState(
        val title: String = "",
        val description: String = "",
        val titleError: String? = null,
        val descriptionError: String? = null,
        val deadline: Instant? = null
    )
}
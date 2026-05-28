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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.co.schwaerzler.maximilian.doit.DoItApplication
import at.co.schwaerzler.maximilian.doit.data.db.TodoRepository
import at.co.schwaerzler.maximilian.doit.data.db.entity.Todo
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** ViewModel for the home screen, exposing the todo lists and bulk-action operations. */
class HomeViewModel(
    private val repository: TodoRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    /**
     * Flow of open and done [TodoSummary] lists derived from a single Room query.
     *
     * Using one query instead of combining two separate flows avoids a race condition where
     * [kotlinx.coroutines.flow.combine] could pair a stale emission from one flow with a fresh
     * emission from the other, momentarily placing the same todo id in both lists and crashing
     * the [androidx.compose.foundation.lazy.LazyColumn] that uses id as a key.
     */
    val todos = repository.getAllSummaries().map { all ->
        val open = all
            .filter { it.state == TodoState.OPEN }
            .sortedWith(compareBy(nullsLast(naturalOrder())) { it.deadlineDateTime })
        val done = all
            .filter { it.state == TodoState.DONE }
            .sortedByDescending { it.creationDateTime }
        Pair(open, done)
    }

    /** Todos that were just deleted and can still be restored via [undoDeleteTodos].
     *  Empty when no undo is pending. */
    private val _pendingUndoTodos = MutableStateFlow<List<Todo>>(emptyList())
    val pendingUndoTodos = _pendingUndoTodos.asStateFlow()

    val todosDone = appPreferences.todosDone
    val doNotShowWidgetDialogAgain = appPreferences.doNotShowWidgetDialogAgain

    fun enableDoNotShowWidgetDialogAgain() {
        appPreferences.enableDoNotShowWidgetDialogAgain()
    }

    /**
     * Toggles [todo] between [TodoState.OPEN] and [TodoState.DONE].
     *
     * Items in any other state (e.g. [TodoState.IN_PROGRESS]) are treated the same as [TodoState.OPEN].
     */
    fun toggleTodoDone(todo: TodoSummary) {
        viewModelScope.launch {
            val newState = if (todo.state == TodoState.OPEN) TodoState.DONE else TodoState.OPEN
            repository.updateState(todo.id, newState)
            if (newState == TodoState.DONE) {
                appPreferences.incrementTodosDoneCount()
            }
        }
    }

    /** Deletes all todos whose primary keys are in [ids] and stores them in [pendingUndoTodos]
     *  so the UI can offer an undo action. */
    fun deleteTodosByIds(ids: List<Int>) {
        viewModelScope.launch {
            _pendingUndoTodos.value = repository.deleteByIdsReturnContents(ids)
        }
    }

    /** Restores the todos captured in [pendingUndoTodos] and clears the pending state. */
    fun undoDeleteTodos() {
        viewModelScope.launch {
            repository.reinsertTodos(_pendingUndoTodos.value)
            _pendingUndoTodos.value = emptyList()
        }
    }

    /** Discards the pending undo state without restoring anything. Call this when the snackbar
     *  times out or is dismissed without the undo action being taken. */
    fun clearPendingTodos() {
        _pendingUndoTodos.value = emptyList()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DoItApplication
                HomeViewModel(
                    repository = app.repository,
                    appPreferences = app.appPreferences
                )
            }
        }
    }
}

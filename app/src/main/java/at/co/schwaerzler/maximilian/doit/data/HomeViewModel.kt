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

import android.util.Log
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
import at.co.schwaerzler.maximilian.doit.util.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Instant

/** ViewModel for the home screen, exposing the todo lists and bulk-action operations. */
class HomeViewModel(
    private val repository: TodoRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val sortOrder = appPreferences.homeScreenSortOrder

    val sortedTodos = combine(repository.getAllSummaries(), sortOrder) { list, order ->
        val comparator = when (order) {
            SortOrder.DEADLINE_SOONEST_FIRST -> compareBy<TodoSummary, Instant?>(nullsLast()) { it.deadlineDateTime }
            SortOrder.ALPHABETICAL_A_Z -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            SortOrder.ALPHABETICAL_Z_A -> compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.title }
            SortOrder.CREATION_DATE_NEWEST_FIRST -> compareByDescending { it.creationDateTime }
            SortOrder.CREATION_DATE_OLDEST_FIRST -> compareBy { it.creationDateTime }
        }
        val sorted = list.sortedWith(comparator)
        sorted.partition { it.state != TodoState.DONE }
    }

    /**
     * Advances [sortOrder] to the next value in the cycle and returns it.
     *
     * The new order is returned directly because the DataStore-backed [sortOrder] flow
     * does not emit the updated value synchronously — callers that need the new label
     * immediately (e.g. to show a snackbar) must use the return value rather than
     * reading [sortOrder] after this call.
     */
    fun rotateSortOrder(): SortOrder {
        val newOrder = when (sortOrder.value) {
            SortOrder.DEADLINE_SOONEST_FIRST -> SortOrder.ALPHABETICAL_A_Z
            SortOrder.ALPHABETICAL_A_Z -> SortOrder.ALPHABETICAL_Z_A
            SortOrder.ALPHABETICAL_Z_A -> SortOrder.CREATION_DATE_NEWEST_FIRST
            SortOrder.CREATION_DATE_NEWEST_FIRST -> SortOrder.CREATION_DATE_OLDEST_FIRST
            SortOrder.CREATION_DATE_OLDEST_FIRST -> SortOrder.DEADLINE_SOONEST_FIRST
        }

        setSortOrder(newOrder)
        return newOrder
    }

    /** Persists [order] to [AppPreferences], updating [sortOrder] asynchronously. */
    fun setSortOrder(order: SortOrder) {
        appPreferences.saveHomeScreenSortOrder(order)
    }

    /** Todos that were just deleted and can still be restored via [undoDeleteTodos].
     *  Empty when no undo is pending. */
    private val _pendingUndoTodos = MutableStateFlow<List<Todo>>(emptyList())
    val pendingUndoTodos = _pendingUndoTodos.asStateFlow()

    val todosDone = appPreferences.todosDone
    val widgetDialogSuppressed = appPreferences.widgetDialogSuppressed

    fun suppressWidgetDialog() {
        appPreferences.saveWidgetDialogSuppressed(true)
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
                appPreferences.incrementTodosDone()
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

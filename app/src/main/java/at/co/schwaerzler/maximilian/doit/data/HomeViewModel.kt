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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.co.schwaerzler.maximilian.doit.DoItApplication
import at.co.schwaerzler.maximilian.doit.data.db.TodoRepository
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.util.appPreferencesDataStore
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import at.co.schwaerzler.maximilian.doit.util.incrementTodosDoneCount
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** ViewModel for the home screen, exposing the todo lists and bulk-action operations. */
class HomeViewModel(
    private val repository: TodoRepository,
    private val appPreferences: DataStore<Preferences>
) : ViewModel() {
    /**
     * Combined flow of open and done [TodoSummary] lists.
     *
     * Emits a new [Pair] whenever either list changes.
     */
    val todos = combine(
        repository.getOpenSummaries(),
        repository.getDoneSummaries()
    ) { open, done -> Pair(open, done) }

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

    /** Permanently deletes all todos whose primary keys are in [ids]. */
    fun deleteTodosByIds(ids: List<Int>) {
        viewModelScope.launch {
            repository.deleteByIds(ids)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DoItApplication
                HomeViewModel(
                    repository = app.repository,
                    appPreferences = app.appPreferencesDataStore
                )
            }
        }
    }
}

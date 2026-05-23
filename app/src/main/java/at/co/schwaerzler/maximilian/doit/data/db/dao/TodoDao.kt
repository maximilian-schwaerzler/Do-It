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

package at.co.schwaerzler.maximilian.doit.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import at.co.schwaerzler.maximilian.doit.data.db.entity.Todo
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import kotlinx.coroutines.flow.Flow

/** Room DAO for all [Todo] CRUD operations. */
@Dao
interface TodoDao {
    /** Returns all todos ordered by deadline (nulls last), then by deadline ascending. */
    @Query("SELECT * FROM todos ORDER BY deadline_timestamp IS NULL ASC, deadline_timestamp ASC")
    fun getAll(): Flow<List<Todo>>

    /** Returns all open todos ordered by deadline (nulls last), then by deadline ascending. */
    @Query("SELECT * FROM todos WHERE state = 'OPEN' ORDER BY deadline_timestamp IS NULL ASC, deadline_timestamp ASC")
    fun getOpen(): Flow<List<Todo>>

    /** Returns all done todos ordered by creation time descending. */
    @Query("SELECT * FROM todos WHERE state = 'DONE' ORDER BY creation_timestamp DESC")
    fun getDone(): Flow<List<Todo>>

    /**
     * Returns [TodoSummary] projections of open todos, ordered by deadline (nulls last),
     * then by deadline ascending. Prefer this over [getOpen] in list views to avoid loading
     * the full [Todo.description] for every row.
     */
    @Query("SELECT id, title, deadline_timestamp, state, creation_timestamp FROM todos WHERE state = 'OPEN' ORDER BY deadline_timestamp IS NULL ASC, deadline_timestamp ASC")
    fun getOpenSummaries(): Flow<List<TodoSummary>>

    /**
     * Returns [TodoSummary] projections of done todos, ordered by creation time descending.
     * Prefer this over [getDone] in list views.
     */
    @Query("SELECT id, title, deadline_timestamp, state, creation_timestamp FROM todos WHERE state = 'DONE' ORDER BY creation_timestamp DESC")
    fun getDoneSummaries(): Flow<List<TodoSummary>>

    /**
     * Returns [TodoSummary] projections of all todos in a single query.
     *
     * Prefer this over combining [getOpenSummaries] + [getDoneSummaries] when both lists are
     * needed together: [kotlinx.coroutines.flow.combine] can pair a stale emission from one
     * flow with a fresh emission from the other, momentarily placing the same id in both lists
     * and crashing any [androidx.compose.foundation.lazy.LazyColumn] that uses it as a key.
     */
    @Query("SELECT id, title, deadline_timestamp, state, creation_timestamp FROM todos")
    fun getAllSummaries(): Flow<List<TodoSummary>>

    /** Returns the todo with the given [id], or `null` if it does not exist. */
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: Int): Todo?

    /** Updates only the [state] column for the todo with the given [id]. */
    @Query("UPDATE todos SET state = :state WHERE id = :id")
    suspend fun updateState(id: Int, state: TodoState)

    @Insert
    suspend fun insert(todo: Todo): Long

    @Update
    suspend fun update(todo: Todo)

    @Delete
    suspend fun delete(todo: Todo)

    @Delete
    suspend fun delete(vararg todo: Todo)

    /** Deletes all todos whose primary keys are in [ids]. */
    @Query("DELETE FROM todos WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)
}
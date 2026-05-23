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

package at.co.schwaerzler.maximilian.doit.data.db

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import at.co.schwaerzler.maximilian.doit.OverviewWidget
import at.co.schwaerzler.maximilian.doit.data.DeadlineNotificationWorker
import at.co.schwaerzler.maximilian.doit.data.db.dao.TodoDao
import at.co.schwaerzler.maximilian.doit.data.db.entity.Todo
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.time.toJavaDuration

class TodoRepository(
    private val appContext: Context,
    private val dao: TodoDao
) {
    private val workManager = WorkManager.getInstance(appContext)

    fun getOpenSummaries(): Flow<List<TodoSummary>> = dao.getOpenSummaries()
    fun getDoneSummaries(): Flow<List<TodoSummary>> = dao.getDoneSummaries()

    suspend fun getById(id: Int): Todo? = dao.getById(id)

    suspend fun insert(todo: Todo) {
        val todoId = dao.insert(todo)
        withContext(NonCancellable) {
            OverviewWidget().updateAll(appContext)
            todo.deadlineDateTime?.let { deadline ->
                scheduleDeadlineNotification(
                    todoId,
                    deadline
                )
            }
        }
    }

    suspend fun update(todo: Todo) {
        dao.update(todo)
        withContext(NonCancellable) {
            OverviewWidget().updateAll(appContext)
            todo.deadlineDateTime?.let { deadline ->
                scheduleDeadlineNotification(
                    todo.id.toLong(),
                    deadline
                )
            } ?: workManager.cancelUniqueWork("deadline_${todo.id}")
        }
    }

    suspend fun delete(todo: Todo) {
        dao.delete(todo)
        withContext(NonCancellable) {
            OverviewWidget().updateAll(appContext)
            workManager.cancelUniqueWork("deadline_${todo.id}")
        }
    }

    suspend fun updateState(todoId: Int, state: TodoState) {
        dao.updateState(todoId, state)
        withContext(NonCancellable) {
            OverviewWidget().updateAll(appContext)
            workManager.cancelUniqueWork("deadline_$todoId")
        }
    }

    suspend fun deleteByIds(ids: List<Int>) {
        dao.deleteByIds(ids)
        withContext(NonCancellable) {
            OverviewWidget().updateAll(appContext)
            ids.forEach { todoId ->
                workManager.cancelUniqueWork("deadline_$todoId")
            }
        }
    }

    private fun scheduleDeadlineNotification(todoId: Long, deadline: Instant) {
        val delay = (deadline - Clock.System.now()) - 30.minutes
        if (delay.isNegative()) return

        val request = OneTimeWorkRequestBuilder<DeadlineNotificationWorker>()
            .setInitialDelay(delay.toJavaDuration())
            .setInputData(workDataOf("todo_id" to todoId))
            .build()

        workManager.enqueueUniqueWork(
            "deadline_$todoId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
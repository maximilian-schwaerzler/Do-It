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

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import at.co.schwaerzler.maximilian.doit.DoItApplication
import at.co.schwaerzler.maximilian.doit.MainActivity
import at.co.schwaerzler.maximilian.doit.R

class DeadlineNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val todoId = inputData.getLong(
            applicationContext.getString(R.string.deadline_notfication_worker_todo_id_input_data),
            0L
        )
        if (todoId == 0L) return Result.failure(workDataOf("failure_reason" to "No todo_id provided"))

        val repository = (applicationContext as DoItApplication).repository
        val todo = repository.getById(todoId.toInt())
            ?: return Result.failure(workDataOf("failure_reason" to "Todo not found in DB"))

        val todoIntent = Intent(applicationContext, MainActivity::class.java).apply {
            setAction(Intent.ACTION_VIEW)
            setData("doit://todo?todoId=$todoId".toUri())
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext, 0, todoIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.todo_deadline_notif_channel_id)
        )
            .setContentTitle(todo.title)
            .setContentText(
                applicationContext.getString(
                    R.string.deadline_notification_content_text,
                    todo.title,
                    applicationContext.resources.getInteger(
                        R.integer.deadline_notification_lead_time_minutes
                    )
                )
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSmallIcon(R.drawable.event_24px)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with Result.failure(workDataOf("failure_reason" to "Notification permission denied"))
            } else {
                notify("deadline", todoId.toInt(), builder.build())
                return@with Result.success()
            }
        }
    }

}
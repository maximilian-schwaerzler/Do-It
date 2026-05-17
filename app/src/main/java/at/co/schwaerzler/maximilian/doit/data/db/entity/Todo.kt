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

package at.co.schwaerzler.maximilian.doit.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Room entity representing a single to-do item stored in the `todos` table.
 *
 * @property id Auto-generated primary key.
 * @property title Short, required label for the item.
 * @property description Optional longer text with details.
 * @property deadlineDateTime Optional point in time by which the item should be completed.
 * @property state Current lifecycle state; defaults to [TodoState.OPEN].
 * @property creationDateTime When the item was created; defaults to the current instant.
 */
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    val title: String,
    val description: String?,
    @ColumnInfo(name = "deadline_timestamp")
    val deadlineDateTime: Instant?,
    val state: TodoState = TodoState.OPEN,
    @ColumnInfo(name = "creation_timestamp")
    val creationDateTime: Instant = Clock.System.now()
)

/** Lifecycle state of a [Todo] item. */
enum class TodoState {
    /** The item has not been started yet. */
    OPEN,
    /** The item is actively being worked on. */
    IN_PROGRESS,
    /** The item has been completed. */
    DONE
}
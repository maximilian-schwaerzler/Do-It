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
import kotlin.time.Instant

/**
 * Lightweight projection of [Todo] used in list views.
 *
 * Contains only the fields needed to render a list item, avoiding loading [Todo.description]
 * for every row when it is not displayed.
 *
 * @property id Primary key of the corresponding [Todo].
 * @property title Short label of the item.
 * @property deadlineDateTime Optional deadline timestamp.
 * @property state Current lifecycle state.
 * @property creationDateTime When the item was created.
 */
data class TodoSummary(
    val id: Int,
    val title: String,
    @ColumnInfo(name = "deadline_timestamp")
    val deadlineDateTime: Instant?,
    val state: TodoState,
    @ColumnInfo(name = "creation_timestamp")
    val creationDateTime: Instant
)
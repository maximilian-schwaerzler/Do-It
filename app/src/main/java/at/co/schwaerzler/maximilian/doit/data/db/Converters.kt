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

import androidx.room.TypeConverter
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.fromEpochSeconds(it) }

    @TypeConverter
    fun instantToTimestamp(date: Instant?): Long? = date?.epochSeconds

    @TypeConverter
    fun toTodoState(value: String): TodoState = enumValueOf(value)

    @TypeConverter
    fun fromTodoState(state: TodoState): String = state.name
}
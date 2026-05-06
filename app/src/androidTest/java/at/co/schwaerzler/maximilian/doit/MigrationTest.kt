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

package at.co.schwaerzler.maximilian.doit

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase
import org.junit.Rule
import org.junit.Test

class MigrationTest {
    private val testDb = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TodoDatabase::class.java
    )

    @Test
    fun migrate1to2() {
        // Create DB at version 1 and insert a row with the old schema
        helper.createDatabase(testDb, 1).apply {
            execSQL("INSERT INTO todos (id, title, description, deadline_timestamp, creation_timestamp) VALUES (1, 'Test', NULL, NULL, 0)")
            close()
        }

        // Run the migration
        helper.runMigrationsAndValidate(testDb, 2, true, TodoDatabase.MIGRATION_1_2)
        // If it doesn't throw, the migration succeeded and the schema matches your entity
    }
}
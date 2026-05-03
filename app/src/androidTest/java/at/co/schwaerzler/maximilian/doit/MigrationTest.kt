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
package at.co.schwaerzler.maximilian.doit.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase
import at.co.schwaerzler.maximilian.doit.data.db.entity.Todo
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditTodoViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var db: TodoDatabase
    private lateinit var viewModel: EditTodoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TodoDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .build()
        viewModel = EditTodoViewModel(db)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        db.close()
    }

    private suspend fun insertTodo(
        title: String,
        description: String? = null,
        deadlineSeconds: Long? = null
    ): Int {
        db.todoDao().insert(
            Todo(
                id = 0,
                title = title,
                description = description,
                deadlineDateTime = deadlineSeconds?.let { Instant.fromEpochSeconds(it) },
                state = TodoState.OPEN,
                creationDateTime = Instant.fromEpochSeconds(0L)
            )
        )
        return db.todoDao().getAll().first().first { it.title == title }.id
    }

    // Validation — blank title

    @Test
    fun saveTodo_blankTitle_returnsFalse() {
        assertFalse(viewModel.saveTodo(null))
    }

    @Test
    fun saveTodo_blankTitle_setsTitleError() {
        viewModel.saveTodo(null)
        assertNotNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun saveTodo_whitespaceTitle_returnsFalse() {
        viewModel.updateTitle("   ")
        assertFalse(viewModel.saveTodo(null))
    }

    // Validation — valid title

    @Test
    fun saveTodo_validTitle_returnsTrue() {
        viewModel.updateTitle("Buy milk")
        assertTrue(viewModel.saveTodo(null))
    }

    @Test
    fun saveTodo_validTitle_titleErrorIsNull() {
        viewModel.updateTitle("Buy milk")
        viewModel.saveTodo(null)
        assertNull(viewModel.uiState.value.titleError)
    }

    // Insert (id == null)

    @Test
    fun saveTodo_nullId_insertsRowInDatabase() = runTest {
        viewModel.updateTitle("Buy milk")
        viewModel.saveTodo(null)
        val all = db.todoDao().getAll().first()
        assertEquals(1, all.size)
        assertEquals("Buy milk", all[0].title)
    }

    @Test
    fun saveTodo_nullId_blankDescriptionStoredAsNull() = runTest {
        viewModel.updateTitle("Task")
        viewModel.saveTodo(null)
        assertNull(db.todoDao().getAll().first()[0].description)
    }

    @Test
    fun saveTodo_nullId_nonBlankDescriptionStored() = runTest {
        viewModel.updateTitle("Task")
        viewModel.updateDescription("Some details")
        viewModel.saveTodo(null)
        assertEquals("Some details", db.todoDao().getAll().first()[0].description)
    }

    @Test
    fun saveTodo_nullId_deadlinePersisted() = runTest {
        val deadline = Instant.fromEpochSeconds(1_700_000_000L)
        viewModel.updateTitle("Task")
        viewModel.updateDeadline(deadline)
        viewModel.saveTodo(null)
        assertEquals(deadline, db.todoDao().getAll().first()[0].deadlineDateTime)
    }

    // Update (id != null)

    @Test
    fun saveTodo_existingId_withoutLoadTodo_returnsFalse() {
        viewModel.updateTitle("New title")
        assertFalse(viewModel.saveTodo(99))
    }

    @Test
    fun saveTodo_existingId_updatesTitle() = runTest {
        val id = insertTodo("Original")
        viewModel.loadTodo(id)
        viewModel.updateTitle("Updated")
        viewModel.saveTodo(id)
        assertEquals("Updated", db.todoDao().getById(id)!!.title)
    }

    @Test
    fun saveTodo_existingId_preservesState() = runTest {
        val id = insertTodo("Task")
        viewModel.loadTodo(id)
        viewModel.updateTitle("Task modified")
        viewModel.saveTodo(id)
        assertEquals(TodoState.OPEN, db.todoDao().getById(id)!!.state)
    }

    @Test
    fun saveTodo_existingId_updatesDeadline() = runTest {
        val id = insertTodo("Task")
        val newDeadline = Instant.fromEpochSeconds(1_700_000_000L)
        viewModel.loadTodo(id)
        viewModel.updateDeadline(newDeadline)
        viewModel.saveTodo(id)
        assertEquals(newDeadline, db.todoDao().getById(id)!!.deadlineDateTime)
    }

    // isModified flow

    @Test
    fun isModified_initially_isFalse() = runTest {
        assertFalse(viewModel.isModified.first())
    }

    @Test
    fun isModified_afterTitleChange_isTrue() = runTest {
        viewModel.updateTitle("Something")
        assertTrue(viewModel.isModified.first())
    }

    @Test
    fun isModified_afterLoadTodo_noChanges_isFalse() = runTest {
        val id = insertTodo("My task")
        viewModel.loadTodo(id)
        assertFalse(viewModel.isModified.first())
    }

    @Test
    fun isModified_afterLoadTodoAndTitleChange_isTrue() = runTest {
        val id = insertTodo("My task")
        viewModel.loadTodo(id)
        viewModel.updateTitle("Changed")
        assertTrue(viewModel.isModified.first())
    }

    @Test
    fun isModified_afterRevertingTitle_isFalse() = runTest {
        val id = insertTodo("Original")
        viewModel.loadTodo(id)
        viewModel.updateTitle("Changed")
        viewModel.updateTitle("Original")
        assertFalse(viewModel.isModified.first())
    }

    // deleteTodo

    @Test
    fun deleteTodo_withLoadedTodo_removesFromDatabase() = runTest {
        val id = insertTodo("To delete")
        viewModel.loadTodo(id)
        viewModel.deleteTodo()
        assertTrue(db.todoDao().getAll().first().isEmpty())
    }

    @Test
    fun deleteTodo_withoutLoadedTodo_doesNothing() = runTest {
        viewModel.deleteTodo()
        assertTrue(db.todoDao().getAll().first().isEmpty())
    }
}

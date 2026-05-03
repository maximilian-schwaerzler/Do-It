package at.co.schwaerzler.maximilian.doit.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase
import at.co.schwaerzler.maximilian.doit.data.db.entity.Todo
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoDaoTest {

    private lateinit var db: TodoDatabase
    private lateinit var dao: TodoDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TodoDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .build()
        dao = db.todoDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun makeTodo(
        title: String,
        state: TodoState = TodoState.OPEN,
        deadlineSeconds: Long? = null,
        creationSeconds: Long = 0L
    ) = Todo(
        id = 0,
        title = title,
        description = null,
        deadlineDateTime = deadlineSeconds?.let { Instant.fromEpochSeconds(it) },
        state = state,
        creationDateTime = Instant.fromEpochSeconds(creationSeconds)
    )

    // CRUD

    @Test
    fun insert_andGetById_returnsTodo() = runTest {
        dao.insert(makeTodo("Buy milk"))
        val result = dao.getById(1)
        assertNotNull(result)
        assertEquals("Buy milk", result!!.title)
        assertEquals(TodoState.OPEN, result.state)
    }

    @Test
    fun insert_multiple_allInGetAll() = runTest {
        dao.insert(makeTodo("Task A"))
        dao.insert(makeTodo("Task B"))
        dao.insert(makeTodo("Task C"))
        assertEquals(3, dao.getAll().first().size)
    }

    @Test
    fun update_changesTitle() = runTest {
        dao.insert(makeTodo("Old title"))
        val inserted = dao.getById(1)!!
        dao.update(inserted.copy(title = "New title"))
        assertEquals("New title", dao.getById(1)!!.title)
    }

    @Test
    fun updateState_toDone_reflectedInGetById() = runTest {
        dao.insert(makeTodo("Task"))
        dao.updateState(1, TodoState.DONE)
        assertEquals(TodoState.DONE, dao.getById(1)!!.state)
    }

    @Test
    fun delete_removesTodo() = runTest {
        dao.insert(makeTodo("Task"))
        val inserted = dao.getById(1)!!
        dao.delete(inserted)
        assertTrue(dao.getAll().first().isEmpty())
    }

    @Test
    fun deleteByIds_removesOnlySpecifiedRows() = runTest {
        dao.insert(makeTodo("Keep"))
        dao.insert(makeTodo("Delete A"))
        dao.insert(makeTodo("Delete B"))
        val all = dao.getAll().first()
        val idsToDelete = all.filter { it.title != "Keep" }.map { it.id }
        dao.deleteByIds(idsToDelete)
        val remaining = dao.getAll().first()
        assertEquals(1, remaining.size)
        assertEquals("Keep", remaining[0].title)
    }

    // State filtering

    @Test
    fun getOpen_returnsOnlyOpenTodos() = runTest {
        dao.insert(makeTodo("Open task", state = TodoState.OPEN))
        dao.insert(makeTodo("Done task", state = TodoState.DONE))
        val open = dao.getOpen().first()
        assertEquals(1, open.size)
        assertEquals(TodoState.OPEN, open[0].state)
    }

    @Test
    fun getDone_returnsOnlyDoneTodos() = runTest {
        dao.insert(makeTodo("Open task", state = TodoState.OPEN))
        dao.insert(makeTodo("Done task", state = TodoState.DONE))
        val done = dao.getDone().first()
        assertEquals(1, done.size)
        assertEquals(TodoState.DONE, done[0].state)
    }

    @Test
    fun getOpenSummaries_returnsOnlyOpen() = runTest {
        dao.insert(makeTodo("Open task", state = TodoState.OPEN))
        dao.insert(makeTodo("Done task", state = TodoState.DONE))
        val summaries = dao.getOpenSummaries().first()
        assertEquals(1, summaries.size)
        assertEquals(TodoState.OPEN, summaries[0].state)
    }

    @Test
    fun getDoneSummaries_returnsOnlyDone() = runTest {
        dao.insert(makeTodo("Open task", state = TodoState.OPEN))
        dao.insert(makeTodo("Done task", state = TodoState.DONE))
        val summaries = dao.getDoneSummaries().first()
        assertEquals(1, summaries.size)
        assertEquals(TodoState.DONE, summaries[0].state)
    }

    // Ordering

    @Test
    fun getOpen_nullDeadlineLast() = runTest {
        dao.insert(makeTodo("No deadline", deadlineSeconds = null))
        dao.insert(makeTodo("Has deadline", deadlineSeconds = 1000L))
        val open = dao.getOpen().first()
        assertEquals(2, open.size)
        assertNotNull(open[0].deadlineDateTime)
        assertNull(open[1].deadlineDateTime)
    }

    @Test
    fun getOpen_earlierDeadlineFirst() = runTest {
        dao.insert(makeTodo("C", deadlineSeconds = 3000L))
        dao.insert(makeTodo("A", deadlineSeconds = 1000L))
        dao.insert(makeTodo("B", deadlineSeconds = 2000L))
        val open = dao.getOpen().first()
        assertEquals(Instant.fromEpochSeconds(1000L), open[0].deadlineDateTime)
        assertEquals(Instant.fromEpochSeconds(2000L), open[1].deadlineDateTime)
        assertEquals(Instant.fromEpochSeconds(3000L), open[2].deadlineDateTime)
    }

    @Test
    fun getDone_mostRecentCreationFirst() = runTest {
        dao.insert(makeTodo("Old", state = TodoState.DONE, creationSeconds = 100L))
        dao.insert(makeTodo("Newest", state = TodoState.DONE, creationSeconds = 300L))
        dao.insert(makeTodo("Middle", state = TodoState.DONE, creationSeconds = 200L))
        val done = dao.getDone().first()
        assertEquals(Instant.fromEpochSeconds(300L), done[0].creationDateTime)
        assertEquals(Instant.fromEpochSeconds(200L), done[1].creationDateTime)
        assertEquals(Instant.fromEpochSeconds(100L), done[2].creationDateTime)
    }

    // Flow reactivity

    @Test
    fun getOpenSummaries_emitsAfterInsert() = runTest {
        val emissions = mutableListOf<List<*>>()
        val job = launch {
            dao.getOpenSummaries().take(2).collect { emissions.add(it) }
        }
        runCurrent() // start collector; it subscribes and receives first (empty) emission
        dao.insert(makeTodo("New task")) // invalidates table; Room emits updated list
        runCurrent() // deliver second emission to collector
        job.join()
        assertEquals(2, emissions.size)
        assertTrue(emissions[0].isEmpty())
        assertEquals(1, emissions[1].size)
    }
}

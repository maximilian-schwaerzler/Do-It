package at.co.schwaerzler.maximilian.doit.data.db

import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    // fromTimestamp (Long? -> Instant?)

    @Test
    fun fromTimestamp_null_returnsNull() {
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun fromTimestamp_zero_returnsEpoch() {
        assertEquals(Instant.fromEpochSeconds(0), converters.fromTimestamp(0L))
    }

    @Test
    fun fromTimestamp_positive_returnsCorrectInstant() {
        assertEquals(Instant.fromEpochSeconds(1_700_000_000L), converters.fromTimestamp(1_700_000_000L))
    }

    @Test
    fun fromTimestamp_negative_returnsPreEpochInstant() {
        assertEquals(Instant.fromEpochSeconds(-3600L), converters.fromTimestamp(-3600L))
    }

    // instantToTimestamp (Instant? -> Long?)

    @Test
    fun instantToTimestamp_null_returnsNull() {
        assertNull(converters.instantToTimestamp(null))
    }

    @Test
    fun instantToTimestamp_epoch_returnsZero() {
        assertEquals(0L, converters.instantToTimestamp(Instant.fromEpochSeconds(0)))
    }

    @Test
    fun instantToTimestamp_positive_returnsEpochSeconds() {
        assertEquals(1_700_000_000L, converters.instantToTimestamp(Instant.fromEpochSeconds(1_700_000_000L)))
    }

    @Test
    fun roundTrip_instantThroughLong_isIdentity() {
        val original = Instant.fromEpochSeconds(1_700_000_000L)
        assertEquals(original, converters.fromTimestamp(converters.instantToTimestamp(original)))
    }

    // fromTodoState (TodoState -> String)

    @Test
    fun fromTodoState_open_returnsOpenString() {
        assertEquals("OPEN", converters.fromTodoState(TodoState.OPEN))
    }

    @Test
    fun fromTodoState_inProgress_returnsInProgressString() {
        assertEquals("IN_PROGRESS", converters.fromTodoState(TodoState.IN_PROGRESS))
    }

    @Test
    fun fromTodoState_done_returnsDoneString() {
        assertEquals("DONE", converters.fromTodoState(TodoState.DONE))
    }

    // toTodoState (String -> TodoState)

    @Test
    fun toTodoState_openString_returnsOpen() {
        assertEquals(TodoState.OPEN, converters.toTodoState("OPEN"))
    }

    @Test
    fun toTodoState_inProgressString_returnsInProgress() {
        assertEquals(TodoState.IN_PROGRESS, converters.toTodoState("IN_PROGRESS"))
    }

    @Test
    fun toTodoState_doneString_returnsDone() {
        assertEquals(TodoState.DONE, converters.toTodoState("DONE"))
    }

    @Test
    fun toTodoState_invalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            converters.toTodoState("INVALID")
        }
    }
}

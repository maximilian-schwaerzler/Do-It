package at.co.schwaerzler.maximilian.doit.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant

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

enum class TodoState {
    OPEN,
    IN_PROGRESS,
    DONE
}
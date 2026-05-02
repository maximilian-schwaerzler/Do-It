package at.co.schwaerzler.maximilian.doit.data.db.entity

import androidx.room.ColumnInfo
import kotlin.time.Instant

data class TodoSummary(
    val id: Int,
    val title: String,
    @ColumnInfo(name = "deadline_timestamp")
    val deadlineDateTime: Instant?,
    val state: TodoState,
    @ColumnInfo(name = "creation_timestamp")
    val creationDateTime: Instant
)
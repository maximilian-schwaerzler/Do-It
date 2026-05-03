package at.co.schwaerzler.maximilian.doit.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import at.co.schwaerzler.maximilian.doit.ui.theme.DoItTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant

@Composable
fun TodoListItem(
    todo: TodoSummary,
    onStateToggle: (newState: Boolean) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val deadlineText = todo.deadlineDateTime?.let { deadline ->
        val local = deadline.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(local)
    }

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = {
                    onLongClick()
                },
                onClick = {
                    onClick()
                },
                onLongClickLabel = "Select this TODO"
            ),
        headlineContent = {
            Text(
                todo.title,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (todo.state == TodoState.DONE) TextDecoration.LineThrough else null
            )
        },
        supportingContent = if (deadlineText != null) {
            { Text("Deadline: $deadlineText") }
        } else null,
        trailingContent = {
            Checkbox(todo.state == TodoState.DONE, onCheckedChange = {
                onStateToggle(it)
            })
        },
        colors = if (selected) ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) else ListItemDefaults.colors()
    )
}

private val previewCreatedAt = Instant.parse("2026-05-01T08:00:00Z")
private val previewDeadline = Instant.parse("2026-05-10T10:00:00Z")

@Preview(showBackground = true)
@Composable
private fun TodoListItemOpenPreview() {
    DoItTheme {
        Surface {
            TodoListItem(
                todo = TodoSummary(1, "Buy groceries", null, TodoState.OPEN, previewCreatedAt),
                onStateToggle = {},
                onClick = {},
                onLongClick = {},
                selected = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoListItemWithDeadlinePreview() {
    DoItTheme {
        Surface {
            TodoListItem(
                todo = TodoSummary(
                    2,
                    "Submit report",
                    previewDeadline,
                    TodoState.OPEN,
                    previewCreatedAt
                ),
                onStateToggle = {},
                onClick = {},
                onLongClick = {},
                selected = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoListItemDonePreview() {
    DoItTheme {
        Surface {
            TodoListItem(
                todo = TodoSummary(3, "Read documentation", null, TodoState.DONE, previewCreatedAt),
                onStateToggle = {},
                onClick = {},
                onLongClick = {},
                selected = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoListItemSelectedPreview() {
    DoItTheme {
        Surface {
            TodoListItem(
                todo = TodoSummary(4, "Buy groceries", null, TodoState.OPEN, previewCreatedAt),
                onStateToggle = {},
                onClick = {},
                onLongClick = {},
                selected = true
            )
        }
    }
}
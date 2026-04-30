package at.co.schwaerzler.maximilian.doit.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import at.co.schwaerzler.maximilian.doit.data.db.entity.Todo
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TodoListItem(
    todo: Todo,
    modifier: Modifier = Modifier
) {
    val deadlineText = todo.deadlineDateTime?.let { deadline ->
        val local = deadline.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(local)
    }

    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = {
            Text(todo.title, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = if (deadlineText != null) {
            { Text("Deadline: $deadlineText") }
        } else null,
        trailingContent = {
            Checkbox(false, onCheckedChange = {})
        }
    )
}
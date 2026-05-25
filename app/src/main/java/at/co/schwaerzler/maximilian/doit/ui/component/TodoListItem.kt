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

package at.co.schwaerzler.maximilian.doit.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.co.schwaerzler.maximilian.doit.R
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import at.co.schwaerzler.maximilian.doit.ui.theme.DoItTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A single row in the todo list.
 *
 * Displays the todo title, optional deadline, and a completion checkbox. Supports selection
 * mode: long-press enters selection, tapping in selection mode toggles the item.
 *
 * @param todo Data to display.
 * @param onStateToggle Called when the checkbox is toggled; receives the new checked state.
 * @param onClick Called on a normal tap (navigate to detail or toggle selection in selection mode).
 * @param onLongClick Called on long-press to enter selection mode.
 * @param selected Whether this item is currently selected (highlighted with [secondaryContainer][androidx.compose.material3.ColorScheme.secondaryContainer]).
 */
@Composable
fun TodoListItem(
    todo: TodoSummary,
    onStateToggle: (newState: Boolean) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val deadlineText = todo.deadlineDateTime?.let { deadline ->
        val local = deadline.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(local)
    }

    val deadlineColor = todo.deadlineDateTime?.let {
        if (it <= Clock.System.now()) {
            MaterialTheme.colorScheme.error
        } else Color.Unspecified
    } ?: Color.Unspecified

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
    val haptic = LocalHapticFeedback.current
    var deleteIconScale by remember { mutableFloatStateOf(1f) }
    val animatedDeleteIconScale by animateFloatAsState(
        targetValue = deleteIconScale,
        label = "deleteIconScale",
    )

    LaunchedEffect(swipeToDismissBoxState.settledValue) {
        if (swipeToDismissBoxState.settledValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    LaunchedEffect(swipeToDismissBoxState.targetValue) {
        if (swipeToDismissBoxState.targetValue == SwipeToDismissBoxValue.EndToStart) {
            haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            deleteIconScale = 1.5f
        } else if (swipeToDismissBoxState.targetValue == SwipeToDismissBoxValue.Settled) {
            deleteIconScale = 1f
        }
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = modifier,
        backgroundContent = {
            when (swipeToDismissBoxState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> {
                    Icon(
                        painterResource(R.drawable.delete_24px),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red)
                            .wrapContentSize(Alignment.CenterEnd)
                            .scale(animatedDeleteIconScale)
                            .padding(12.dp),
                        tint = Color.White,
                    )
                }
                else -> {}
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onLongClick = {
                        onLongClick()
                    },
                    onClick = {
                        onClick()
                    },
                    onLongClickLabel = stringResource(R.string.select_this_todo)
                ),
            headlineContent = {
                Text(
                    todo.title,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (todo.state == TodoState.DONE) TextDecoration.LineThrough else null
                )
            },
            supportingContent = if (deadlineText != null) {
                {
                    Text(
                        stringResource(R.string.deadline_template, deadlineText),
                        color = deadlineColor
                    )
                }
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
                onDelete = {},
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
                onDelete = {},
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
                onDelete = {},
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
                onDelete = {},
                selected = true
            )
        }
    }
}
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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import at.co.schwaerzler.maximilian.doit.data.TodoRepository
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import kotlin.time.Clock

class OverviewWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val context = LocalContext.current
                val repo = remember { TodoRepository(context, TodoDatabase.getDatabase(context).todoDao()) }
                val todos by repo.getOpenSummaries().collectAsState(emptyList())
                OverviewWidgetContent(todos)
            }
        }
    }
}

@Composable
internal fun OverviewWidgetContent(todos: List<TodoSummary>) {
    val context = LocalContext.current
    Scaffold(GlanceModifier.fillMaxSize().padding(8.dp)) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Text(
                text = context.getString(R.string.app_name),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            )
            if (todos.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = context.getString(R.string.nothing_to_do_empty_text),
                        style = TextStyle(color = GlanceTheme.colors.onSurface)
                    )
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    items(todos) { todo ->
                        Text(
                            text = "- ${todo.title}",
                            maxLines = 1,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 14.sp,
                            ),
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 100)
@Composable
fun OverviewWidgetPreview() {
    GlanceTheme {
        OverviewWidgetContent(
            todos = listOf(
                TodoSummary(1, "Buy milk", null, TodoState.OPEN, Clock.System.now()),
                TodoSummary(2, "Write code", null, TodoState.OPEN, Clock.System.now())
            )
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 100)
@Composable
fun OverviewWidgetEmptyPreview() {
    GlanceTheme {
        OverviewWidgetContent(todos = emptyList())
    }
}

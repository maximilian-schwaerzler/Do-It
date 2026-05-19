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
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import at.co.schwaerzler.maximilian.doit.data.TodoRepository
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoState
import at.co.schwaerzler.maximilian.doit.data.db.entity.TodoSummary
import kotlin.time.Clock
import androidx.core.net.toUri

class OverviewWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val context = LocalContext.current
                val repo = remember {
                    TodoRepository(
                        context,
                        TodoDatabase.getDatabase(context).todoDao()
                    )
                }
                val todos by repo.getOpenSummaries().collectAsState(emptyList())
                OverviewWidgetContent(todos)
            }
        }
    }

    @Composable
    fun OverviewWidgetContent(todos: List<TodoSummary>) {
        val context = LocalContext.current
        Scaffold(
            titleBar = {
                TitleBar(
                    title = context.getString(R.string.app_name),
                    startIcon = ImageProvider(R.drawable.check_24px),
                    // TODO: Not sure if this is necessary with the shortcut
//                    actions = {
//                        CircleIconButton(
//                            ImageProvider(R.drawable.add_24px),
//                            null,
//                            actionStartActivity(Intent(Intent.ACTION_VIEW, "doit://todo".toUri())),
//                            backgroundColor = null
//                        )
//                    }
                )
            },
            modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())
        ) {
            if (todos.isEmpty()) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        ImageProvider(R.drawable.check_circle_24px),
                        modifier = GlanceModifier.size(35.dp).padding(bottom = 8.dp),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                    )
                    Text(
                        text = context.getString(R.string.nothing_to_do_empty_text),
                        style = TextStyle(color = GlanceTheme.colors.onSurface)
                    )
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier
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
@Preview(widthDp = 250, heightDp = 250)
@Composable
fun OverviewWidgetPreview() {
    GlanceTheme {
        OverviewWidget().OverviewWidgetContent(
            todos = listOf(
                TodoSummary(1, "Buy milk", null, TodoState.OPEN, Clock.System.now()),
                TodoSummary(2, "Write code", null, TodoState.OPEN, Clock.System.now())
            )
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 250)
@Composable
fun OverviewWidgetEmptyPreview() {
    GlanceTheme {
        OverviewWidget().OverviewWidgetContent(todos = emptyList())
    }
}

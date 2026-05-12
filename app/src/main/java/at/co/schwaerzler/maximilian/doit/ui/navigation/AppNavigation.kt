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

package at.co.schwaerzler.maximilian.doit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.co.schwaerzler.maximilian.doit.ui.navigation.screen.EditTodoScreen
import at.co.schwaerzler.maximilian.doit.ui.navigation.screen.HomeScreen
import at.co.schwaerzler.maximilian.doit.ui.navigation.screen.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class EditTodo(val todoId: Int? = null)

@Serializable
object Settings

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home, modifier = modifier) {
        composable<Home> {
            HomeScreen(
                onAddTodo = {
                    navController.navigate(EditTodo(todoId = null))
                },
                onClickTodo = { todoId ->
                    navController.navigate(EditTodo(todoId = todoId))
                }
            )
        }

        composable<EditTodo> { backStackEntry ->
            val route = backStackEntry.toRoute<EditTodo>()
            EditTodoScreen(
                route.todoId,
                navigateBack = {
                    navController.popBackStack()
                }, onCancel = {
                    navController.popBackStack()
                })
        }

        composable<Settings> {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
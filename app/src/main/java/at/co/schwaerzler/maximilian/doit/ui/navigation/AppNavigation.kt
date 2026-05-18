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

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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

/** Navigation route for the home/list screen. */
@Serializable
object Home

/**
 * Navigation route for the add/edit screen.
 *
 * @property todoId Primary key of the todo to edit, or `null` to create a new one.
 */
@Serializable
data class EditTodo(val todoId: Int? = null)

/** Navigation route for the settings screen. */
@Serializable
object Settings

/**
 * Root composable that sets up the [NavHost] with all app destinations.
 *
 * Navigation actions are passed into each screen as callbacks; the [NavController][androidx.navigation.NavController] is not
 * exposed outside this function.
 */
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
                },
                onClickSettings = {
                    navController.navigate(Settings)
                }
            )
        }

        composable<EditTodo>(
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { -it / 3 } },
            popEnterTransition = { slideInHorizontally { -it / 3 } },
            popExitTransition = { slideOutHorizontally { it } },
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<EditTodo>()
            EditTodoScreen(
                route.todoId,
                navigateBack = {
                    navController.popBackStack()
                }, onCancel = {
                    navController.popBackStack()
                })
        }

        composable<Settings>(
            enterTransition = { fadeIn() + scaleIn(initialScale = 0.92f) },
            popExitTransition = { fadeOut() + scaleOut(targetScale = 0.92f) }
        ) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
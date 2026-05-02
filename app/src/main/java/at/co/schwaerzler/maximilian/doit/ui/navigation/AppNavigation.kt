package at.co.schwaerzler.maximilian.doit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.co.schwaerzler.maximilian.doit.ui.navigation.screen.EditTodoScreen
import at.co.schwaerzler.maximilian.doit.ui.navigation.screen.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class EditTodo(val todoId: Int? = null)

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
            EditTodoScreen(route.todoId, navigateBack = {
                navController.popBackStack()
            })
        }
    }
}
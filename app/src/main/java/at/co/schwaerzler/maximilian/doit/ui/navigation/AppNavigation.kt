package at.co.schwaerzler.maximilian.doit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.co.schwaerzler.maximilian.doit.ui.navigation.screen.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
object Home

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home, modifier = modifier) {
        composable<Home> {
            HomeScreen()
        }
    }
}
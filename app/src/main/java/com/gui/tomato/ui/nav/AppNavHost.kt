package com.gui.tomato.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gui.tomato.ui.screens.*

@Composable
fun AppNavHost(nav: NavHostController, start: String = "home") {
    NavHost(navController = nav, startDestination = start) {
        composable("home") { HomeScreen(nav) }
        composable("tasks") { TasksScreen(nav) }
        composable("stats") { StatsScreen(nav) }
        composable("settings") { SettingsScreen(nav) }
        composable("taskDetail/{id}") { back ->
            val id = back.arguments?.getLong("id") ?: 0L
            TaskDetailScreen(nav, id)
        }
        composable("taskEdit/{id}") { back ->
            val id = back.arguments?.getLong("id") ?: 0L
            TaskEditScreen(nav, id)
        }
        composable("presetEdit") { PresetEditScreen(nav) }
    }
}

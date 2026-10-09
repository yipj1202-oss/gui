package com.gui.tomato.ui.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gui.tomato.R
import com.gui.tomato.ui.screens.*

@Composable
fun AppNavHost(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    start: String = "home"
) {
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("home", "tasks", "stats")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                nav.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "tasks",
                        onClick = {
                            if (currentRoute != "tasks") {
                                nav.navigate("tasks") {
                                    popUpTo("home")
                                }
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.nav_tasks)) },
                        label = { Text(stringResource(R.string.nav_tasks)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "stats",
                        onClick = {
                            if (currentRoute != "stats") {
                                nav.navigate("stats") {
                                    popUpTo("home")
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.nav_stats)) },
                        label = { Text(stringResource(R.string.nav_stats)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen(nav) }
            composable("tasks") { TasksScreen(nav) }
            composable("stats") { StatsScreen(nav) }
            composable("settings") { SettingsScreen(nav) }
            composable("taskDetail/{id}") { back ->
                val id = back.arguments?.getString("id")?.toLongOrNull() ?: 0L
                TaskDetailScreen(nav, id)
            }
            composable("taskEdit/{id}") { back ->
                val id = back.arguments?.getString("id")?.toLongOrNull() ?: 0L
                TaskEditScreen(nav, id)
            }
            composable("presetEdit") { PresetEditScreen(nav) }
        }
    }
}

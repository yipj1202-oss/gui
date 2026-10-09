package com.gui.tomato.ui.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gui.tomato.R
import com.gui.tomato.ui.screens.*

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)

@Composable
fun AppNavHost(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    start: String = "home"
) {
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("home", "tasks", "stats")
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val barColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = if (isDark) Color(0xFF8A8A8A) else MaterialTheme.colorScheme.outline

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = barColor) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                nav.navigate("home") { popUpTo("home") { inclusive = true } }
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = selectedColor.copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "tasks",
                        onClick = {
                            if (currentRoute != "tasks") {
                                nav.navigate("tasks") { popUpTo("home") }
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.nav_tasks)) },
                        label = { Text(stringResource(R.string.nav_tasks)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = selectedColor.copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "stats",
                        onClick = {
                            if (currentRoute != "stats") {
                                nav.navigate("stats") { popUpTo("home") }
                            }
                        },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.nav_stats)) },
                        label = { Text(stringResource(R.string.nav_stats)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = selectedColor.copy(alpha = 0.15f)
                        )
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
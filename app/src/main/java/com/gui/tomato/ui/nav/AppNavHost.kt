package com.gui.tomato.ui.nav

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer
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
    val showBottomBar = currentRoute in listOf("home", "tasks", "calendar", "stats")
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
                                nav.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = stringResource(R.string.nav_tasks)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_tasks)) },
                        colors = navItemColors(selectedColor, unselectedColor)
                    )
                    NavigationBarItem(
                        selected = currentRoute == "tasks",
                        onClick = {
                            if (currentRoute != "tasks") {
                                nav.navigate("tasks") {
                                    popUpTo("home")
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Filled.Timer, contentDescription = "番茄") },
                        label = { Text("番茄") },
                        colors = navItemColors(selectedColor, unselectedColor)
                    )
                    NavigationBarItem(
                        selected = currentRoute == "calendar",
                        onClick = {
                            if (currentRoute != "calendar") {
                                nav.navigate("calendar") {
                                    popUpTo("home")
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Filled.DateRange, contentDescription = "日历") },
                        label = { Text("日历") },
                        colors = navItemColors(selectedColor, unselectedColor)
                    )
                    NavigationBarItem(
                        selected = currentRoute == "stats",
                        onClick = {
                            if (currentRoute != "stats") {
                                nav.navigate("stats") {
                                    popUpTo("home")
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Filled.BarChart,
                                contentDescription = stringResource(R.string.nav_stats)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_stats)) },
                        colors = navItemColors(selectedColor, unselectedColor)
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 50 },
                            animationSpec = tween(260, easing = FastOutSlowInEasing)
                        )
            },
            exitTransition = {
                fadeOut(tween(180, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(tween(220, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                fadeOut(tween(180, easing = FastOutSlowInEasing))
            }
        ) {
            composable("home") { TasksScreen(nav) }
            composable("tasks") { HomeScreen(nav) }
            composable("calendar") { CalendarScreen(nav) }
            composable("stats") { StatsScreen(nav) }

            composable(
                "settings",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(200))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(200))
                }
            ) { SettingsScreen(nav) }

            composable(
                "taskDetail/{id}",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(200))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(200))
                }
            ) { back ->
                val id = back.arguments?.getString("id")?.toLongOrNull() ?: 0L
                TaskDetailScreen(nav, id)
            }

            composable(
                "taskEdit/{id}",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(200))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(200))
                }
            ) { back ->
                val id = back.arguments?.getString("id")?.toLongOrNull() ?: 0L
                TaskEditScreen(nav, id)
            }

            composable(
                "presetEdit",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(200))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(200))
                }
            ) { PresetEditScreen(nav) }
        }
    }
}

@Composable
private fun navItemColors(selected: Color, unselected: Color) = NavigationBarItemDefaults.colors(
    selectedIconColor = selected,
    selectedTextColor = selected,
    unselectedIconColor = unselected,
    unselectedTextColor = unselected,
    indicatorColor = selected.copy(alpha = 0.15f)
)
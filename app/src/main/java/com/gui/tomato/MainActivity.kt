package com.gui.tomato

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gui.tomato.ui.nav.AppNavHost
import com.gui.tomato.ui.prefs.AppPrefs
import com.gui.tomato.ui.screens.SplashScreen
import com.gui.tomato.ui.theme.GuiTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val prefs = remember { AppPrefs(applicationContext) }
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                // small delay so splash is visible
                kotlinx.coroutines.delay(400)
                showSplash = false
            }
            if (showSplash) SplashScreen() else MainScaffold(prefs)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(prefs: AppPrefs) {
    GuiTheme(themeIndex = prefs.theme) {
        val nav = rememberNavController()
        val entry = nav.currentBackStackEntryAsState()
        val current = entry.value?.destination?.route
        val tabs = listOf("home" to "主页", "tasks" to "任务", "stats" to "统计")
        val onTab = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.BarChart)
        Box(Modifier.fillMaxSize()) {
            AppNavHost(nav)
            NavigationBar {
                tabs.forEachIndexed { i, (route, label) ->
                    NavigationBarItem(
                        selected = current == route,
                        onClick = { nav.navigate(route) { popUpTo("home") } },
                        icon = { Icon(onTab[i], contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}
package com.gui.tomato

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gui.tomato.ui.nav.AppNavHost
import com.gui.tomato.ui.prefs.AppPrefs
import com.gui.tomato.ui.screens.SplashScreen
import com.gui.tomato.ui.theme.GuiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val prefs = remember { AppPrefs(applicationContext) }
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(400)
                showSplash = false
            }
            if (showSplash) SplashScreen() else MainScaffold(prefs)
        }
    }
}

@Composable
private fun MainScaffold(prefs: AppPrefs) {
    GuiTheme(themeIndex = prefs.theme) {
        val nav = rememberNavController()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavHost(nav)
        }
    }
}

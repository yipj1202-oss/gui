package com.gui.tomato

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.compose.rememberNavController
import com.gui.tomato.ui.nav.AppNavHost
import com.gui.tomato.ui.prefs.AppPrefs
import com.gui.tomato.ui.screens.SplashScreen
import com.gui.tomato.ui.theme.GuiTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = AppPrefs(newBase)
        val lang = prefs.lang
        val locale = if (lang == 1) Locale.ENGLISH else Locale.CHINESE
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = applicationContext as GuiApp
            val prefs = app.prefs

            var splashVisible by remember { mutableStateOf(true) }
            var mainVisible by remember { mutableStateOf(false) }
            val mainAlpha = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                // 等 Splash 里 slogan 完全显示 + 停留
                kotlinx.coroutines.delay(2900)
                mainVisible = true
                mainAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(420, easing = FastOutSlowInEasing)
                )
                splashVisible = false
            }

            Box(Modifier.fillMaxSize()) {
                // 底层：主界面
                if (mainVisible) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = mainAlpha.value }
                    ) {
                        MainScaffold(prefs)
                    }
                }

                // 上层：Splash（盖在主界面之上）
                if (splashVisible) {
                    SplashScreen()
                }
            }
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
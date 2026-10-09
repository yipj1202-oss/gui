package com.gui.tomato.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Color(0xFFE4572E), onPrimary = Color.White,
    secondary = Color(0xFF2E7D4F), background = Color(0xFFF7F5F0), surface = Color(0xFFFFFFFF)
)
val DarkColors = darkColorScheme(
    primary = Color(0xFFE4572E), onPrimary = Color.White,
    secondary = Color(0xFF5FBF8F), background = Color(0xFF12141A), surface = Color(0xFF1B1E27)
)

@Composable
fun GuiTheme(themeIndex: Int, content: @Composable () -> Unit) {
    val isDark = when (themeIndex) {
        1 -> true
        2 -> isSystemInDarkTheme()
        else -> false
    }
    MaterialTheme(colorScheme = if (isDark) DarkColors else LightColors, content = content)
}
package com.gui.tomato

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.gui.tomato.ui.nav.AppNavHost
import com.gui.tomato.ui.prefs.AppPrefs
import com.gui.tomato.ui.screens.SplashScreen
import com.gui.tomato.ui.theme.GuiTheme
import com.gui.tomato.util.UpdateChecker
import java.util.Locale

private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

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

            // 更新检查结果
            var updateInfo by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }

            LaunchedEffect(Unit) {
                // 等 Splash 显示完
                kotlinx.coroutines.delay(2900)
                mainVisible = true
                mainAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(420, easing = FastOutSlowInEasing)
                )
                splashVisible = false

                // 主界面显示后，后台静默检查更新
                val info = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    UpdateChecker.check(app)
                }
                if (info != null) updateInfo = info
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

                // 上层：Splash
                if (splashVisible) {
                    SplashScreen()
                }
            }

            // 更新提示弹窗
            updateInfo?.let { info ->
                UpdateDialog(
                    info = info,
                    onDismiss = { updateInfo = null },
                    onGoToDownload = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.htmlUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        } catch (_: Exception) {
                        }
                        updateInfo = null
                    }
                )
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

@Composable
private fun UpdateDialog(
    info: UpdateChecker.UpdateInfo,
    onDismiss: () -> Unit,
    onGoToDownload: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(18.dp),
        title = {
            Column {
                Text(
                    text = "发现新版本",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 4.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = info.versionName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.sp,
                    color = OrangeStart
                )
            }
        },
        text = {
            Column {
                if (info.body.isNotBlank()) {
                    Text(
                        text = info.body.take(300),
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFF8A8A8A)
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Text(
                    text = "去 GitHub 下载最新版 APK",
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFF8A8A8A)
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd)))
                    .clickable { onGoToDownload() }
                    .padding(horizontal = 22.dp, vertical = 10.dp)
            ) {
                Text(
                    "前往下载",
                    color = Color.White,
                    fontSize = 13.sp,
                    letterSpacing = 3.sp
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onDismiss() }
                    .padding(horizontal = 22.dp, vertical = 10.dp)
            ) {
                Text(
                    "以后再说",
                    color = Color(0xFF8A8A8A),
                    fontSize = 13.sp,
                    letterSpacing = 3.sp
                )
            }
        }
    )
}
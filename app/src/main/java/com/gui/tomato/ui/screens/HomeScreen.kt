package com.gui.tomato.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.engine.Snapshot
import com.gui.tomato.engine.State
import com.gui.tomato.ui.widgets.ProgressRing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var snap by remember { mutableStateOf(app.repo.current()) }

    LaunchedEffect(Unit) {
        while (true) {
            snap = app.repo.current()
            kotlinx.coroutines.delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("晷") },
                actions = {
                    IconButton(onClick = { nav.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                app.repo.engine.preset.name,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable { nav.navigate("presetEdit") }
            )
            ProgressRing(progress = ringProgress(snap), sizeDp = 240, label = formatRemain(snap))
            Spacer(Modifier.height(12.dp))
            if (snap.taskId != null) Text("进行中", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            when (snap.state) {
                State.IDLE -> Button(onClick = {
                    app.repo.start(taskId = null)
                    app.serviceStart()
                }) { Text("开始专注") }
                State.FOCUS -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { app.repo.skip() }) { Text("跳过") }
                    Button(onClick = { app.repo.finish(); app.serviceStop() }) { Text("结束") }
                }
                else -> OutlinedButton(onClick = { app.repo.skip() }) { Text("跳过休息") }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "今日 ${app.todayFocusCountSafe()} 番茄 · ${app.todayFocusMinutesSafe()} 分钟",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable { nav.navigate("stats") }
            )
        }
    }
}

private fun ringProgress(s: Snapshot): Float =
    if (s.plannedMs == 0L) 0f else 1f - (s.remainMs.toFloat() / s.plannedMs.toFloat())
private fun formatRemain(s: Snapshot): String =
    if (s.plannedMs == 0L) "未开始" else String.format("%02d:%02d", s.remainMs / 60000, (s.remainMs % 60000) / 1000)
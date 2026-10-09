package com.gui.tomato.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var range by remember { mutableIntStateOf(0) }
    var rows by remember { mutableStateOf<List<com.gui.tomato.data.TaskStatRow>>(emptyList()) }
    var hours by remember { mutableStateOf<List<com.gui.tomato.data.HourStatRow>>(emptyList()) }

    LaunchedEffect(range) {
        val now = System.currentTimeMillis()
        val from = when (range) {
            0 -> {
                val c = java.util.Calendar.getInstance()
                c.set(java.util.Calendar.HOUR_OF_DAY, 0)
                c.set(java.util.Calendar.MINUTE, 0)
                c.set(java.util.Calendar.SECOND, 0)
                c.set(java.util.Calendar.MILLISECOND, 0)
                c.timeInMillis
            }
            1 -> now - 7 * 86400_000L
            else -> now - 30 * 86400_000L
        }
        rows = app.byTaskSafe(from, now)
        hours = app.byHourSafe(from, now)
    }

    val maxTask = rows.maxOfOrNull { it.focusMin }?.takeIf { it > 0 } ?: 1
    val maxHour = hours.maxOfOrNull { it.focusMin }?.takeIf { it > 0 } ?: 1
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val secondary = MaterialTheme.colorScheme.secondary

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("统计") })
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("今天", "本周", "本月").forEachIndexed { i, l ->
                    FilterChip(
                        selected = range == i,
                        onClick = { range = i },
                        label = { Text(l) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("按任务分布", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (rows.isEmpty()) {
                Text(
                    "暂无专注数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                rows.take(10).forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            r.taskName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(90.dp)
                        )
                        Canvas(
                            modifier = Modifier
                                .height(16.dp)
                                .weight(1f)
                        ) {
                            val w = size.width
                            drawRect(color = outline, size = Size(w, 12f))
                            drawRect(
                                color = primary,
                                size = Size(w * (r.focusMin.toFloat() / maxTask).coerceIn(0.02f, 1f), 12f)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${r.focusMin}分 · ${r.pomodoros}个",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("时段分布", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (hours.isEmpty()) {
                Text(
                    "暂无数值数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                hours.forEach { h ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "%02d时".format(h.hour),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(45.dp)
                        )
                        Canvas(
                            modifier = Modifier
                                .height(12.dp)
                                .weight(1f)
                        ) {
                            val w = size.width
                            drawRect(color = outline, size = Size(w, 8f))
                            drawRect(
                                color = secondary,
                                size = Size(w * (h.focusMin.toFloat() / maxHour).coerceIn(0.0f, 1f), 8f)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${h.focusMin}分",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(50.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

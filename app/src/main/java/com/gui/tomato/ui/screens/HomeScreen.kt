package com.gui.tomato.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.data.entities.PomodoroPreset
import com.gui.tomato.data.entities.Task
import com.gui.tomato.engine.Snapshot
import com.gui.tomato.engine.State
import com.gui.tomato.ui.widgets.ProgressRing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var snap by remember { mutableStateOf(app.repo.current()) }

    var showPresetSheet by remember { mutableStateOf(false) }
    var showTaskSheet by remember { mutableStateOf(false) }

    var presetList by remember { mutableStateOf<List<PomodoroPreset>>(emptyList()) }
    var activeTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var currentTaskTitle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            snap = app.repo.current()
            kotlinx.coroutines.delay(1000)
        }
    }

    LaunchedEffect(showPresetSheet) {
        if (showPresetSheet) {
            presetList = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                app.db.presetDao().listAll()
            }
        }
    }

    LaunchedEffect(Unit) {
        app.db.taskDao().activeFlow().collect { activeTasks = it }
    }

    LaunchedEffect(snap.taskId) {
        val id = snap.taskId
        if (id != null) {
            val task = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                app.db.taskDao().getById(id)
            }
            currentTaskTitle = task?.title ?: "未知任务"
        } else {
            currentTaskTitle = null
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
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Surface(
                onClick = { showPresetSheet = true },
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "方案：${app.repo.engine.preset.name} (${app.repo.engine.preset.focusMin}+${app.repo.engine.preset.shortBreakMin}) ▾",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            ProgressRing(progress = ringProgress(snap), sizeDp = 240, label = formatRemain(snap))
            Spacer(Modifier.height(16.dp))
            if (snap.taskId != null) {
                Text(
                    text = "正在专注: ${currentTaskTitle ?: "加载中..."}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(16.dp))
            when (snap.state) {
                State.IDLE -> Button(onClick = { showTaskSheet = true }) {
                    Text("开始专注")
                }
                else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (snap.isPaused) {
                        Button(onClick = { app.repo.resume(); snap = app.repo.current() }) {
                            Text("继续")
                        }
                    } else {
                        OutlinedButton(onClick = { app.repo.pause(); snap = app.repo.current() }) {
                            Text("暂停")
                        }
                    }
                    OutlinedButton(onClick = { app.repo.skip(); snap = app.repo.current() }) {
                        Text("跳过")
                    }
                    Button(onClick = { app.repo.finish(); app.serviceStop(); snap = app.repo.current() }) {
                        Text("结束")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "今日 ${app.todayFocusCountSafe()} 番茄 · ${app.todayFocusMinutesSafe()} 分钟",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable { nav.navigate("stats") }
            )
        }
    }

    if (showPresetSheet) {
        ModalBottomSheet(onDismissRequest = { showPresetSheet = false }) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("选择番茄方案", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = {
                        showPresetSheet = false
                        nav.navigate("presetEdit")
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("新建方案")
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(presetList) { p ->
                        val isCurrent = app.repo.engine.preset.id == p.id || app.repo.engine.preset.name == p.name
                        ListItem(
                            headlineContent = { Text(p.name) },
                            supportingContent = { Text("专注 ${p.focusMin}分 · 短休 ${p.shortBreakMin}分 · 长休 ${p.longBreakMin}分 (每${p.longBreakEvery}个)") },
                            trailingContent = { if (isCurrent) Text("使用中", color = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.clickable {
                                app.repo.applyPreset(p)
                                snap = app.repo.current()
                                showPresetSheet = false
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showTaskSheet) {
        ModalBottomSheet(onDismissRequest = { showTaskSheet = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("选择关联任务", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                ListItem(
                    headlineContent = { Text("无任务 (直接专注)") },
                    modifier = Modifier.clickable {
                        showTaskSheet = false
                        app.repo.start(taskId = null)
                        app.serviceStart()
                    }
                )
                HorizontalDivider()
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(activeTasks) { task ->
                        ListItem(
                            headlineContent = { Text(task.title) },
                            supportingContent = { if (task.note.isNotEmpty()) Text(task.note) },
                            modifier = Modifier.clickable {
                                showTaskSheet = false
                                app.repo.start(taskId = task.id)
                                app.serviceStart()
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

private fun ringProgress(s: Snapshot): Float =
    if (s.plannedMs == 0L) 0f else 1f - (s.remainMs.toFloat() / s.plannedMs.toFloat())
private fun formatRemain(s: Snapshot): String =
    if (s.plannedMs == 0L) "未开始" else String.format("%02d:%02d", s.remainMs / 60000, (s.remainMs % 60000) / 1000)

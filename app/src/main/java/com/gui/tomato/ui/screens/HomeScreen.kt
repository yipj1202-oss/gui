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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
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

    val unknownTaskStr = stringResource(R.string.home_task_unknown)

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
            currentTaskTitle = task?.title ?: unknownTaskStr
        } else {
            currentTaskTitle = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = { nav.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.home_settings))
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
                    text = stringResource(R.string.home_preset_prefix) + "${app.repo.engine.preset.name} (${app.repo.engine.preset.focusMin}+${app.repo.engine.preset.shortBreakMin}) " + stringResource(R.string.home_preset_suffix),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            ProgressRing(progress = ringProgress(snap), sizeDp = 240, label = formatRemain(snap, stringResource(R.string.home_not_started)))
            Spacer(Modifier.height(16.dp))
            if (snap.taskId != null) {
                Text(
                    text = stringResource(R.string.home_focusing_on, currentTaskTitle ?: stringResource(R.string.home_task_loading)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(16.dp))
            when (snap.state) {
                State.IDLE -> Button(onClick = { showTaskSheet = true }) {
                    Text(stringResource(R.string.home_start_focus))
                }
                else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (snap.isPaused) {
                        Button(onClick = { app.repo.resume(); snap = app.repo.current() }) {
                            Text(stringResource(R.string.home_resume))
                        }
                    } else {
                        OutlinedButton(onClick = { app.repo.pause(); snap = app.repo.current() }) {
                            Text(stringResource(R.string.home_pause))
                        }
                    }
                    OutlinedButton(onClick = { app.repo.skip(); snap = app.repo.current() }) {
                        Text(stringResource(R.string.home_skip))
                    }
                    Button(onClick = { app.repo.finish(); app.serviceStop(); snap = app.repo.current() }) {
                        Text(stringResource(R.string.home_finish))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.home_today_summary, app.todayFocusCountSafe(), app.todayFocusMinutesSafe()),
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
                    Text(stringResource(R.string.home_choose_preset), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = {
                        showPresetSheet = false
                        nav.navigate("presetEdit")
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.home_new_preset))
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(presetList) { p ->
                        val isCurrent = app.repo.engine.preset.id == p.id || app.repo.engine.preset.name == p.name
                        ListItem(
                            headlineContent = { Text(p.name) },
                            supportingContent = { Text(stringResource(R.string.home_preset_detail, p.focusMin, p.shortBreakMin, p.longBreakMin, p.longBreakEvery)) },
                            trailingContent = { if (isCurrent) Text(stringResource(R.string.home_in_use), color = MaterialTheme.colorScheme.primary) },
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
                Text(stringResource(R.string.home_choose_task), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                ListItem(
                    headlineContent = { Text(stringResource(R.string.home_no_task)) },
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
private fun formatRemain(s: Snapshot, notStartedStr: String): String =
    if (s.plannedMs == 0L) notStartedStr else String.format("%02d:%02d", s.remainMs / 60000, (s.remainMs % 60000) / 1000)

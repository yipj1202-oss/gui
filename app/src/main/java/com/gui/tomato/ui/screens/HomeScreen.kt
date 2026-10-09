package com.gui.tomato.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.entities.PomodoroPreset
import com.gui.tomato.data.entities.Task
import com.gui.tomato.engine.Snapshot
import com.gui.tomato.engine.State

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)
private val DarkTrack = Color(0xFF2A2A2A)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)

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
            currentTaskTitle = task?.title ?: "?"
        } else {
            currentTaskTitle = null
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.home_title),
                        color = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = { nav.navigate("settings") }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.home_settings),
                            tint = if (isDark) DarkTextSub else MaterialTheme.colorScheme.onBackground
                        )
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
            Spacer(Modifier.height(4.dp))

            StatusChip(snap.state, snap.isPaused, isDark)

            Spacer(Modifier.height(10.dp))

            PresetChip(
                name = app.repo.engine.preset.name,
                focusMin = app.repo.engine.preset.focusMin,
                shortBreakMin = app.repo.engine.preset.shortBreakMin,
                isDark = isDark,
                onClick = { showPresetSheet = true }
            )

            Spacer(Modifier.height(28.dp))

            DashboardRing(snap = snap, isDark = isDark)

            Spacer(Modifier.height(16.dp))

            if (snap.taskId != null) {
                Text(
                    stringResource(
                        R.string.home_focusing_on,
                        currentTaskTitle ?: stringResource(R.string.home_task_loading)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(24.dp))

            ControlButtons(
                snap = snap,
                onStart = { showTaskSheet = true },
                onPause = { app.repo.pause(); snap = app.repo.current() },
                onResume = { app.repo.resume(); snap = app.repo.current() },
                onSkip = { app.repo.skip(); snap = app.repo.current() },
                onFinish = { app.repo.finish(); app.serviceStop(); snap = app.repo.current() }
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = stringResource(
                    R.string.home_today_summary,
                    app.todayFocusCountSafe(),
                    app.todayFocusMinutesSafe()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .clickable { nav.navigate("stats") }
                    .padding(16.dp)
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
                            supportingContent = {
                                Text(stringResource(R.string.home_preset_detail, p.focusMin, p.shortBreakMin, p.longBreakMin, p.longBreakEvery))
                            },
                            trailingContent = {
                                if (isCurrent) Text(stringResource(R.string.home_in_use), color = MaterialTheme.colorScheme.primary)
                            },
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

@Composable
private fun StatusChip(state: State, isPaused: Boolean, isDark: Boolean) {
    val label = when {
        isPaused -> "PAUSED"
        state == State.FOCUS -> "FOCUS"
        state == State.SHORT_BREAK -> "SHORT BREAK"
        state == State.LONG_BREAK -> "LONG BREAK"
        else -> "READY"
    }
    val dotColor = when {
        isPaused -> Color(0xFFFFB300)
        state == State.FOCUS -> MaterialTheme.colorScheme.primary
        state == State.SHORT_BREAK || state == State.LONG_BREAK -> Color(0xFF4CAF50)
        else -> if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isDark) DarkCard else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = if (isDark) DarkTextSub else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PresetChip(
    name: String,
    focusMin: Int,
    shortBreakMin: Int,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isDark) DarkCard else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$name  ·  $focusMin + $shortBreakMin",
            fontSize = 12.sp,
            color = if (isDark) DarkTextSub else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "▾",
            fontSize = 10.sp,
            color = if (isDark) DarkTextSub else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DashboardRing(snap: Snapshot, isDark: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val trackColor = if (isDark) DarkTrack else Color(0xFFEAEAEA)
    val textColor = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val subColor = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    val progress = if (snap.plannedMs == 0L) 0f
    else (1f - snap.remainMs.toFloat() / snap.plannedMs.toFloat()).coerceIn(0f, 1f)

    val timeText = if (snap.plannedMs == 0L) {
        stringResource(R.string.home_not_started)
    } else {
        String.format("%02d:%02d", snap.remainMs / 60000, (snap.remainMs % 60000) / 1000)
    }

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 5.dp.toPx()
            val radius = size.minDimension / 2f - 28.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            val tickCount = 60
            for (i in 0 until tickCount) {
                val angleRad = Math.toRadians((i * 360f / tickCount - 90f).toDouble())
                val isMajor = i % 5 == 0
                val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val startR = radius + 20.dp.toPx()
                val endR = startR + tickLen
                val sx = center.x + (startR * Math.cos(angleRad)).toFloat()
                val sy = center.y + (startR * Math.sin(angleRad)).toFloat()
                val ex = center.x + (endR * Math.cos(angleRad)).toFloat()
                val ey = center.y + (endR * Math.sin(angleRad)).toFloat()
                val done = i.toFloat() / tickCount <= progress
                val tickColor = if (done) primary else trackColor
                drawLine(
                    color = tickColor,
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    color = primary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                fontSize = if (snap.plannedMs == 0L) 28.sp else 60.sp,
                fontWeight = FontWeight.Thin,
                color = textColor,
                letterSpacing = 2.sp
            )
            if (snap.plannedMs > 0L) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = subColor
                )
            }
        }
    }
}

@Composable
private fun ControlButtons(
    snap: Snapshot,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    if (snap.state == State.IDLE) {
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 14.dp)
        ) {
            Text(
                stringResource(R.string.home_start_focus),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (snap.isPaused) {
                Button(
                    onClick = onResume,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.home_resume))
                }
            } else {
                OutlinedButton(
                    onClick = onPause,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.home_pause), color = MaterialTheme.colorScheme.primary)
                }
            }
            OutlinedButton(
                onClick = onSkip,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(stringResource(R.string.home_skip))
            }
            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.home_finish))
            }
        }
    }
}
package com.gui.tomato.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.entities.PomodoroPreset
import com.gui.tomato.data.entities.Task
import com.gui.tomato.engine.Snapshot
import com.gui.tomato.engine.State
import com.gui.tomato.ui.components.StaggerIn
import com.gui.tomato.util.Haptic

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)
private val DarkTrack = Color(0xFF262626)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)
private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

// 超出起跑线时的"深色"
private val DeepOrangeStart = Color(0xFFA83A1A)
private val DeepOrangeEnd = Color(0xFFD9622B)

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

    // ── 仪表自检 ──
    var selfCheckActive by remember {
        mutableStateOf(snap.state == State.IDLE && snap.plannedMs == 0L)
    }
    val selfCheckAnim = remember { Animatable(0f) }

    LaunchedEffect(snap.state) {
        if (snap.state != State.IDLE) selfCheckActive = false
    }

    LaunchedEffect(Unit) {
        if (selfCheckActive) {
            selfCheckAnim.snapTo(0f)
            kotlinx.coroutines.delay(380)

            // ── 启动：短震
            if (app.prefs.vibrate) Haptic.tap(ctx)

            // 冲劲：560ms 从 0 拉到 110%
            selfCheckAnim.animateTo(
                targetValue = 1.10f,
                animationSpec = keyframes {
                    durationMillis = 560
                    0.55f at 160
                    0.90f at 320
                    1.10f at 560
                }
            )

            // ── 撞顶：双击（最有"撞击感"的一下）
            if (app.prefs.vibrate) Haptic.doubleTap(ctx)

            // 撞击回弹（快速）
            selfCheckAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(130, easing = FastOutSlowInEasing)
            )

            // 顶峰短停
            kotlinx.coroutines.delay(160)

            // 原路倒回（提速）
            selfCheckAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(420, easing = FastOutSlowInEasing)
            )

            // ── 回位：短震
            if (app.prefs.vibrate) Haptic.tap(ctx)

            kotlinx.coroutines.delay(80)
            selfCheckActive = false
        }
    }

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
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    val ringSelfCheck: Float? = if (selfCheckActive) selfCheckAnim.value else null
    val numSelfCheck: Float? = if (selfCheckActive) {
        if (selfCheckAnim.value >= 0.95f) 1f else selfCheckAnim.value
    } else null

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    StaggerIn(index = 0, startOffsetY = 14.dp) {
                        Text(
                            text = "晷",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Thin,
                            letterSpacing = 2.sp,
                            style = MaterialTheme.typography.titleLarge.copy(
                                brush = Brush.linearGradient(listOf(OrangeStart, OrangeEnd))
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    StaggerIn(index = 1, startOffsetY = 14.dp) {
                        IconButton(onClick = { nav.navigate("settings") }) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.home_settings),
                                tint = textSub
                            )
                        }
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
            Spacer(Modifier.height(8.dp))

            StaggerIn(index = 2) {
                StatusLine(snap.state, snap.isPaused, isDark)
            }

            Spacer(Modifier.height(16.dp))

            StaggerIn(index = 3) {
                PresetLine(
                    name = app.repo.engine.preset.name,
                    focusMin = app.repo.engine.preset.focusMin,
                    shortBreakMin = app.repo.engine.preset.shortBreakMin,
                    isDark = isDark,
                    onClick = { showPresetSheet = true }
                )
            }

            Spacer(Modifier.height(36.dp))

            StaggerIn(index = 4, startOffsetX = 40.dp) {
                DashboardRing(
                    snap = snap,
                    isDark = isDark,
                    presetFocusMin = app.repo.engine.preset.focusMin,
                    selfCheckRing = ringSelfCheck,
                    selfCheckNum = numSelfCheck,
                )
            }

            Spacer(Modifier.height(20.dp))

            if (snap.taskId != null) {
                StaggerIn(index = 5) {
                    Text(
                        stringResource(
                            R.string.home_focusing_on,
                            currentTaskTitle ?: stringResource(R.string.home_task_loading)
                        ),
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = textSub
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            StaggerIn(index = 6) {
                ControlButtons(
                    snap = snap,
                    onStart = { showTaskSheet = true },
                    onPause = { app.repo.pause(); snap = app.repo.current() },
                    onResume = { app.repo.resume(); snap = app.repo.current() },
                    onSkip = { app.repo.skip(); snap = app.repo.current() },
                    onFinish = { app.repo.finish(); app.serviceStop(); snap = app.repo.current() }
                )
            }

            Spacer(Modifier.weight(1f))

            StaggerIn(index = 7, startOffsetY = 16.dp) {
                Text(
                    text = stringResource(
                        R.string.home_today_summary,
                        app.todayFocusCountSafe(),
                        app.todayFocusMinutesSafe()
                    ),
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = textSub,
                    modifier = Modifier
                        .clickable { nav.navigate("stats") }
                        .padding(20.dp)
                )
            }
        }
    }

    if (showPresetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPresetSheet = false },
            containerColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface
        ) {
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
                                if (isCurrent) Text(stringResource(R.string.home_in_use), color = OrangeStart)
                            },
                            modifier = Modifier.clickable {
                                app.repo.applyPreset(p)
                                snap = app.repo.current()
                                showPresetSheet = false
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }
            }
        }
    }

    if (showTaskSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTaskSheet = false },
            containerColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface
        ) {
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
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
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
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusLine(state: State, isPaused: Boolean, isDark: Boolean) {
    val label = when {
        isPaused -> "PAUSED"
        state == State.FOCUS -> "FOCUS"
        state == State.SHORT_BREAK -> "SHORT BREAK"
        state == State.LONG_BREAK -> "LONG BREAK"
        else -> "READY"
    }
    val dotColor = when {
        isPaused -> Color(0xFFFFB300)
        state == State.FOCUS -> OrangeStart
        state == State.SHORT_BREAK || state == State.LONG_BREAK -> Color(0xFF4CAF50)
        else -> Color(0xFF666666)
    }

    val infinite = rememberInfiniteTransition(label = "breath")
    val breathAlpha by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathAlpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .alpha(if (state != State.IDLE) breathAlpha else 1f)
                .background(dotColor)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 5.sp,
            color = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun PresetLine(
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
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$name  ·  $focusMin + $shortBreakMin",
            fontSize = 11.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp,
            color = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "▾",
            fontSize = 9.sp,
            color = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun DashboardRing(
    snap: Snapshot,
    isDark: Boolean,
    presetFocusMin: Int,
    selfCheckRing: Float?,
    selfCheckNum: Float?,
) {
    val trackColor = if (isDark) DarkTrack else Color(0xFFEAEAEA)
    val textColor = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val subColor = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    val realProgress = if (snap.plannedMs == 0L) 0f
    else (1f - snap.remainMs.toFloat() / snap.plannedMs.toFloat()).coerceIn(0f, 1f)

    val rawProgress = selfCheckRing ?: realProgress
    val mainProgress = rawProgress.coerceIn(0f, 1f)

    val deepness = ((rawProgress - 1f) / 0.10f).coerceIn(0f, 1f)

    val arcA = lerp(OrangeStart, DeepOrangeStart, deepness)
    val arcB = lerp(OrangeEnd, DeepOrangeEnd, deepness)
    val arcColors = listOf(arcA, arcB, arcA)

    val timeText: String = when {
        selfCheckNum != null -> {
            val totalMs = presetFocusMin * 60_000L
            val shown = (totalMs * selfCheckNum).toLong()
            String.format("%02d:%02d", shown / 60000, (shown % 60000) / 1000)
        }
        snap.plannedMs == 0L -> {
            String.format("%02d:%02d", presetFocusMin, 0)
        }
        else -> {
            String.format("%02d:%02d", snap.remainMs / 60000, (snap.remainMs % 60000) / 1000)
        }
    }

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val radius = size.minDimension / 2f - 30.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            val tickCount = 60
            for (i in 0 until tickCount) {
                val angleRad = Math.toRadians((i * 360f / tickCount - 90f).toDouble())
                val isMajor = i % 5 == 0
                val tickLen = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
                val startR = radius + 22.dp.toPx()
                val endR = startR + tickLen
                val sx = center.x + (startR * Math.cos(angleRad)).toFloat()
                val sy = center.y + (startR * Math.sin(angleRad)).toFloat()
                val ex = center.x + (endR * Math.cos(angleRad)).toFloat()
                val ey = center.y + (endR * Math.sin(angleRad)).toFloat()
                val done = i.toFloat() / tickCount <= mainProgress
                val tickColor = if (done) arcA else trackColor
                drawLine(
                    color = tickColor,
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
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

            if (mainProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(colors = arcColors),
                    startAngle = -90f,
                    sweepAngle = 360f * mainProgress,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = timeText,
                fontSize = 62.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                color = textColor
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${(mainProgress * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                color = subColor
            )
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
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd)))
                    .padding(horizontal = 56.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.home_start_focus),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 4.sp,
                    color = Color.White
                )
            }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (snap.isPaused) {
                PrimaryPill(stringResource(R.string.home_resume), onResume)
            } else {
                GhostPill(stringResource(R.string.home_pause), onPause)
            }
            GhostPill(stringResource(R.string.home_skip), onSkip)
            PrimaryPill(stringResource(R.string.home_finish), onFinish)
        }
    }
}

@Composable
private fun PrimaryPill(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd)))
                .padding(horizontal = 28.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = 13.sp, letterSpacing = 2.sp, color = Color.White)
        }
    }
}

@Composable
private fun GhostPill(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(label, fontSize = 13.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Light)
    }
}
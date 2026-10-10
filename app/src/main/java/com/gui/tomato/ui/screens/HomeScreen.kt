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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
private val DarkSheetRow = Color(0xFF141414)
private val DarkTrack = Color(0xFF262626)
private val DarkDivider = Color(0xFF262626)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)
private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

private val DeepOrangeStart = Color(0xFFA83A1A)
private val DeepOrangeEnd = Color(0xFFD9622B)

// 判断某预设是不是"当前使用中"：只按 id 匹配；id 无效时按属性严格匹配
private fun isSamePreset(engine: PomodoroPreset, p: PomodoroPreset): Boolean {
    return if (engine.id > 0L && p.id > 0L) {
        engine.id == p.id
    } else {
        engine.name == p.name &&
                engine.focusMin == p.focusMin &&
                engine.shortBreakMin == p.shortBreakMin &&
                engine.longBreakMin == p.longBreakMin &&
                engine.longBreakEvery == p.longBreakEvery
    }
}

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

    // 自检动画由 trigger 驱动：首次进页面（IDLE 且未开始）跑一次，切方案时再跑一次
    var selfCheckTrigger by remember {
        mutableIntStateOf(if (snap.state == State.IDLE && snap.plannedMs == 0L) 1 else 0)
    }
    val selfCheckAnim = remember { Animatable(0f) }
    var selfCheckActive by remember { mutableStateOf(selfCheckTrigger > 0) }

    // 自检过程中若用户启动了番茄钟 → 打断，不再显示自检覆盖环
    LaunchedEffect(snap.state) {
        if (snap.state != State.IDLE && selfCheckActive) selfCheckActive = false
    }

    // 从任务页长按跳过来时：自动弹方案选择
    val pendingStartId = app.pendingStartTaskId
    LaunchedEffect(pendingStartId) {
        if (pendingStartId != null) {
            showPresetSheet = true
        }
    }

    LaunchedEffect(selfCheckTrigger) {
        if (selfCheckTrigger == 0) return@LaunchedEffect

        selfCheckActive = true
        selfCheckAnim.snapTo(0f)
        kotlinx.coroutines.delay(240)

        if (app.prefs.vibrate) Haptic.tap(ctx)

        selfCheckAnim.animateTo(
            targetValue = 1.10f,
            animationSpec = keyframes {
                durationMillis = 380
                0.55f at 100
                0.90f at 200
                1.10f at 380
            }
        )

        if (app.prefs.vibrate) Haptic.doubleTap(ctx)

        selfCheckAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(85, easing = FastOutSlowInEasing)
        )

        kotlinx.coroutines.delay(80)

        selfCheckAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(280, easing = FastOutSlowInEasing)
        )

        if (app.prefs.vibrate) Haptic.tap(ctx)

        kotlinx.coroutines.delay(40)
        selfCheckActive = false
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
    val dividerColor = if (isDark) DarkDivider else Color(0xFFE5E5E5)

    val ringSelfCheck: Float? = if (selfCheckActive) selfCheckAnim.value else null
    val numSelfCheck: Float? = if (selfCheckActive) {
        if (selfCheckAnim.value >= 0.95f) 1f else selfCheckAnim.value
    } else null

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    StaggerIn(index = 0, startOffsetY = 14.dp) {
                        IconButton(onClick = { nav.navigate("settings") }) {
                            Text(
                                text = "晷",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Thin,
                                letterSpacing = 2.sp,
                                style = TextStyle(
                                    brush = Brush.linearGradient(listOf(OrangeStart, OrangeEnd))
                                )
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

            StaggerIn(index = 1) {
                StatusLine(snap.state, snap.isPaused, isDark)
            }

            Spacer(Modifier.height(16.dp))

            StaggerIn(index = 2) {
                PresetLine(
                    name = app.repo.engine.preset.name,
                    focusMin = app.repo.engine.preset.focusMin,
                    shortBreakMin = app.repo.engine.preset.shortBreakMin,
                    isDark = isDark,
                    onClick = { showPresetSheet = true }
                )
            }

            Spacer(Modifier.height(36.dp))

            StaggerIn(index = 3, startOffsetX = 40.dp) {
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
                StaggerIn(index = 4) {
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

            StaggerIn(index = 5) {
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

            StaggerIn(index = 6, startOffsetY = 16.dp) {
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

    // ── 预设选择弹窗（美化版）──
    if (showPresetSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showPresetSheet = false
                // 若是从任务页跳来启动，取消就清空
                app.pendingStartTaskId = null
            },
            containerColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 12.dp, bottom = 6.dp)
                        .size(width = 36.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDark) Color(0xFF3A3A3A) else Color(0xFFD8D8D8))
                )
            }
        ) {
            Column(Modifier.padding(horizontal = 22.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.home_choose_preset),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Thin,
                        letterSpacing = 5.sp,
                        color = textMain
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "PRESETS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 5.sp,
                        color = textSub,
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(dividerColor)
                )

                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    itemsIndexed(presetList) { idx, p ->
                        val isCurrent = isSamePreset(app.repo.engine.preset, p)
                        StaggerIn(
                            index = idx,
                            modifier = Modifier.fillMaxWidth(),
                            startOffsetX = 24.dp
                        ) {
                            PresetSheetRow(
                                index = idx + 1,
                                preset = p,
                                isCurrent = isCurrent,
                                isDark = isDark,
                                textMain = textMain,
                                textSub = textSub,
                                dividerColor = dividerColor,
                                onClick = {
                                    val pendingId = app.pendingStartTaskId

                                    app.repo.applyPreset(p)

                                    // 若从任务长按跳来：立即启动该任务
                                    if (pendingId != null) {
                                        app.repo.start(taskId = pendingId)
                                        app.serviceStart()
                                        app.pendingStartTaskId = null
                                    }

                                    snap = app.repo.current()
                                    showPresetSheet = false
                                    selfCheckTrigger++
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd))
                        )
                        .clickable {
                            showPresetSheet = false
                            app.pendingStartTaskId = null
                            nav.navigate("presetEdit")
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.home_new_preset),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 5.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))
            }
        }
    }

    // ── 任务选择弹窗（保留原样，下次单独美化）──
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

// ── 预设弹窗单行 ──
@Composable
private fun PresetSheetRow(
    index: Int,
    preset: PomodoroPreset,
    isCurrent: Boolean,
    isDark: Boolean,
    textMain: Color,
    textSub: Color,
    dividerColor: Color,
    onClick: () -> Unit,
) {
    val rowBg = if (isDark) DarkSheetRow else Color(0xFFF5F1EA)
    val borderColor = if (isCurrent) OrangeStart.copy(alpha = 0.55f) else dividerColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rowBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%02d".format(index),
            fontSize = 28.sp,
            fontWeight = FontWeight.Thin,
            letterSpacing = 1.sp,
            style = TextStyle(
                brush = Brush.linearGradient(listOf(OrangeStart, OrangeEnd))
            ),
            modifier = Modifier.width(46.dp)
        )
        Box(
            Modifier
                .width(1.dp)
                .height(32.dp)
                .background(dividerColor)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = preset.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = textMain
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = stringResource(
                    R.string.home_preset_detail,
                    preset.focusMin,
                    preset.shortBreakMin,
                    preset.longBreakMin,
                    preset.longBreakEvery
                ),
                fontSize = 9.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp,
                color = textSub
            )
        }
        if (isCurrent) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(OrangeStart.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_in_use),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp,
                    color = OrangeStart
                )
            }
        }
    }
}

@Composable
private fun StatusLine(state: State, isPaused: Boolean, isDark: Boolean) {
    val label = when {
        isPaused -> stringResource(R.string.state_paused)
        state == State.FOCUS -> stringResource(R.string.state_focus)
        state == State.SHORT_BREAK -> stringResource(R.string.state_short_break)
        state == State.LONG_BREAK -> stringResource(R.string.state_long_break)
        else -> stringResource(R.string.state_ready)
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
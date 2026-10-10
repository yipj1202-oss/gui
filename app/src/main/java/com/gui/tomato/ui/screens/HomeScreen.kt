package com.gui.tomato.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

// OVERCLOCK 烫橙
private val OverStart = Color(0xFFC01F0F)
private val OverEnd = Color(0xFFE4572E)

// 自检发光：与过载主题同色（烫橙红），不割裂
private val GoldColor = OverStart

private val ScramblePool = "!@#$%&*+-=?<>[]{}|/\\~^;:アイウエオカキ".toList()

private const val OVER_WORD = "OVER"
private const val CLOCK_WORD = "CLOCK"

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

    val isOverclock by app.repo.isOverclock.collectAsState()

    var selfCheckTrigger by remember {
        mutableIntStateOf(if (snap.state == State.IDLE && snap.plannedMs == 0L) 1 else 0)
    }
    val selfCheckAnim = remember { Animatable(0f) }
    var selfCheckActive by remember { mutableStateOf(selfCheckTrigger > 0) }

    // ── OVERCLOCK 触发特效 ──
    var overclockIntroActive by remember { mutableStateOf(false) }
    val overclockRingAnim = remember { Animatable(0f) }
    val overclockTextAlpha = remember { Animatable(0f) }
    val overclockRevealCount = remember { Animatable(0f) }
    var overclockScrambleSeed by remember { mutableIntStateOf(0) }

    val overclockTheme = isOverclock || overclockIntroActive
    val themeStart = if (overclockTheme) OverStart else OrangeStart
    val themeEnd = if (overclockTheme) OverEnd else OrangeEnd

    LaunchedEffect(snap.state) {
        if (snap.state != State.IDLE && selfCheckActive) selfCheckActive = false
    }

    val pendingStartId = app.pendingStartTaskId
    LaunchedEffect(pendingStartId) {
        if (pendingStartId != null) {
            showPresetSheet = true
        }
    }

    // 自检动画
    LaunchedEffect(selfCheckTrigger) {
        if (selfCheckTrigger == 0) return@LaunchedEffect
        selfCheckActive = true
        selfCheckAnim.snapTo(0f)
        delay(240)
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
        selfCheckAnim.animateTo(1f, tween(85, easing = FastOutSlowInEasing))
        delay(80)
        selfCheckAnim.animateTo(0f, tween(280, easing = FastOutSlowInEasing))
        if (app.prefs.vibrate) Haptic.tap(ctx)
        delay(40)
        selfCheckActive = false
    }

    // overclock 乱码 seed
    LaunchedEffect(overclockIntroActive) {
        while (overclockIntroActive) {
            overclockScrambleSeed++
            delay(60)
        }
    }

    // overclock 触发动画（~3s）
    LaunchedEffect(overclockIntroActive) {
        if (!overclockIntroActive) return@LaunchedEffect

        if (app.prefs.vibrate) Haptic.buzz(ctx, 180)

        overclockRingAnim.animateTo(
            targetValue = 1.15f,
            animationSpec = keyframes {
                durationMillis = 720
                0.30f at 60
                0.65f at 200
                0.92f at 380
                1.05f at 520
                1.15f at 720
            }
        )

        if (app.prefs.vibrate) Haptic.tap(ctx)

        delay(120)

        overclockRingAnim.animateTo(1.0f, tween(220, easing = FastOutSlowInEasing))

        if (app.prefs.vibrate) Haptic.doubleTap(ctx)
        launch { overclockTextAlpha.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
        launch {
            overclockRevealCount.snapTo(0f)
            overclockRevealCount.animateTo(9f, tween(560, easing = LinearEasing))
        }

        delay(1150)

        if (app.prefs.vibrate) Haptic.buzz(ctx, 90)

        launch { overclockRingAnim.animateTo(0f, tween(540, easing = FastOutSlowInEasing)) }
        launch { overclockTextAlpha.animateTo(0f, tween(540, easing = FastOutSlowInEasing)) }
        overclockRevealCount.animateTo(0f, tween(540, easing = FastOutSlowInEasing))

        delay(220)

        if (app.prefs.vibrate) Haptic.buzz(ctx, 240)

        val preset = presetList.firstOrNull { it.id == app.prefs.overclockPresetId }
        if (preset != null) {
            app.repo.activateOverclock(preset, null)
            app.serviceStart()
            snap = app.repo.current()
        }
        overclockIntroActive = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            snap = app.repo.current()
            delay(1000)
        }
    }
    LaunchedEffect(Unit) {
        presetList = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            app.db.presetDao().listAll()
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

    val ringOverride: Float? = when {
        selfCheckActive -> selfCheckAnim.value
        overclockIntroActive -> overclockRingAnim.value
        else -> null
    }
    val numOverride: Float? = when {
        selfCheckActive -> if (selfCheckAnim.value >= 0.95f) 1f else selfCheckAnim.value
        else -> null
    }

    val overclockGlow = if (overclockIntroActive) {
        (overclockRingAnim.value / 1.15f).coerceIn(0f, 1f)
    } else 0f

    val overclockReady = app.prefs.overclockPresetId > 0L

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
                                    brush = Brush.linearGradient(listOf(themeStart, themeEnd))
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
                StatusLine(
                    state = snap.state,
                    isPaused = snap.isPaused,
                    isDark = isDark,
                    isOverclock = isOverclock || overclockIntroActive,
                )
            }

            Spacer(Modifier.height(16.dp))

            StaggerIn(index = 2) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PresetLine(
                        name = app.repo.engine.preset.name,
                        focusMin = app.repo.engine.preset.focusMin,
                        shortBreakMin = app.repo.engine.preset.shortBreakMin,
                        isDark = isDark,
                        onClick = { showPresetSheet = true }
                    )
                    Spacer(Modifier.width(8.dp))
                    OverclockPill(
                        isOverclock = isOverclock,
                        ready = overclockReady,
                        canActivate = overclockReady && snap.state == State.IDLE && !overclockIntroActive,
                        themeStart = themeStart,
                        themeEnd = themeEnd,
                        textSub = textSub,
                        onClick = {
                            if (overclockReady && snap.state == State.IDLE && !overclockIntroActive) {
                                overclockIntroActive = true
                            }
                        },
                        onExit = {
                            app.repo.exitOverclock()
                            snap = app.repo.current()
                        }
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            StaggerIn(index = 3, startOffsetX = 40.dp) {
                DashboardRing(
                    snap = snap,
                    isDark = isDark,
                    presetFocusMin = app.repo.engine.preset.focusMin,
                    selfCheckRing = ringOverride,
                    selfCheckNum = numOverride,
                    overclockIntro = overclockIntroActive,
                    overclockTextAlpha = overclockTextAlpha.value,
                    overclockRevealCount = overclockRevealCount.value,
                    overclockGlow = overclockGlow,
                    overclockScrambleSeed = overclockScrambleSeed,
                    themeStart = themeStart,
                    themeEnd = themeEnd,
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
                    themeStart = themeStart,
                    themeEnd = themeEnd,
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
                    color = if (overclockTheme) themeStart else textSub,
                    modifier = Modifier
                        .clickable { nav.navigate("stats") }
                        .padding(20.dp)
                )
            }
        }
    }

    // ── 预设选择弹窗（A 方向）──
    if (showPresetSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showPresetSheet = false
                app.pendingStartTaskId = null
            },
            containerColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 14.dp, bottom = 4.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDark) Color(0xFF3A3A3A) else Color(0xFFD8D8D8))
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.home_choose_preset),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 6.sp,
                    color = textMain,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "P R E S E T S",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 6.sp,
                    color = textSub,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(dividerColor)
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(presetList) { idx, p ->
                        val isCurrent = isSamePreset(app.repo.engine.preset, p)
                        StaggerIn(
                            index = idx,
                            modifier = Modifier.fillMaxWidth(),
                            startOffsetX = 24.dp
                        ) {
                            PresetCard(
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
                        .background(Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd)))
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

    // ── 任务选择弹窗（A 方向，同套语言）──
    if (showTaskSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTaskSheet = false },
            containerColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 14.dp, bottom = 4.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDark) Color(0xFF3A3A3A) else Color(0xFFD8D8D8))
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.home_choose_task),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 6.sp,
                    color = textMain,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "T A S K S",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 6.sp,
                    color = textSub,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(dividerColor)
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // "无任务"卡片（虚线感：浅描边 + 空心点）
                    item {
                        StaggerIn(
                            index = 0,
                            modifier = Modifier.fillMaxWidth(),
                            startOffsetX = 24.dp
                        ) {
                            NoTaskCard(
                                isDark = isDark,
                                textMain = textMain,
                                textSub = textSub,
                                onClick = {
                                    showTaskSheet = false
                                    app.repo.start(taskId = null)
                                    app.serviceStart()
                                }
                            )
                        }
                    }

                    // 任务卡片
                    itemsIndexed(activeTasks) { idx, task ->
                        StaggerIn(
                            index = idx + 1,
                            modifier = Modifier.fillMaxWidth(),
                            startOffsetX = 24.dp
                        ) {
                            TaskSelectCard(
                                task = task,
                                isDark = isDark,
                                textMain = textMain,
                                textSub = textSub,
                                dividerColor = dividerColor,
                                onClick = {
                                    showTaskSheet = false
                                    app.repo.start(taskId = task.id)
                                    app.serviceStart()
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
            }
        }
    }
}

// ── 预设卡片（A 方向）──
@Composable
private fun PresetCard(
    index: Int,
    preset: PomodoroPreset,
    isCurrent: Boolean,
    isDark: Boolean,
    textMain: Color,
    textSub: Color,
    dividerColor: Color,
    onClick: () -> Unit,
) {
    val cardBg = if (isDark) DarkSheetRow else Color(0xFFF5F1EA)
    val borderColor = if (isCurrent) OrangeStart.copy(alpha = 0.65f) else dividerColor
    val borderWidth = if (isCurrent) 1.2.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%02d".format(index),
            fontSize = 30.sp,
            fontWeight = FontWeight.Thin,
            letterSpacing = 1.sp,
            style = TextStyle(
                brush = Brush.linearGradient(
                    if (isCurrent) listOf(OrangeStart, OrangeEnd)
                    else listOf(textSub, textSub)
                )
            ),
            modifier = Modifier.width(50.dp)
        )
        Box(
            Modifier
                .width(1.dp)
                .height(36.dp)
                .background(dividerColor)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = preset.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = textMain
            )
            Spacer(Modifier.height(4.dp))
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
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(OrangeStart, OrangeEnd))
                    )
            )
        }
    }
}

// ── 任务卡片（A 方向）──
@Composable
private fun TaskSelectCard(
    task: Task,
    isDark: Boolean,
    textMain: Color,
    textSub: Color,
    dividerColor: Color,
    onClick: () -> Unit,
) {
    val cardBg = if (isDark) DarkSheetRow else Color(0xFFF5F1EA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, dividerColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 实心橙点（代表"有任务"）
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(OrangeStart, OrangeEnd)))
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = textMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (task.note.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = task.note,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    color = textSub,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── "无任务"卡片 ──
@Composable
private fun NoTaskCard(
    isDark: Boolean,
    textMain: Color,
    textSub: Color,
    onClick: () -> Unit,
) {
    // 无任务：透明底 + 浅描边 + 空心点（跟"有任务"视觉区分）
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .border(
                1.dp,
                textSub.copy(alpha = 0.30f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 空心点
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .border(
                    1.dp,
                    textSub.copy(alpha = 0.55f),
                    CircleShape
                )
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_no_task),
                fontSize = 15.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp,
                color = textSub
            )
        }
    }
}

// ── OVERCLOCK 入口小胶囊 ──
@Composable
private fun OverclockPill(
    isOverclock: Boolean,
    ready: Boolean,
    canActivate: Boolean,
    themeStart: Color,
    themeEnd: Color,
    textSub: Color,
    onClick: () -> Unit,
    onExit: () -> Unit,
) {
    val clickable = if (isOverclock) true else canActivate

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .then(
                when {
                    isOverclock -> Modifier.background(
                        Brush.horizontalGradient(listOf(OverStart, OverEnd))
                    )
                    canActivate -> Modifier
                        .background(themeStart.copy(alpha = 0.14f))
                        .border(1.dp, themeStart.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                    else -> Modifier
                        .background(Color.Transparent)
                        .border(1.dp, textSub.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                }
            )
            .clickable(enabled = clickable) {
                if (isOverclock) onExit() else onClick()
            }
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = if (isOverclock) "⚡ 取消过载" else ("⚡ " + stringResource(R.string.home_overclock_entry)),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 3.sp,
            color = when {
                isOverclock -> Color.White
                canActivate -> themeStart
                else -> textSub.copy(alpha = 0.5f)
            }
        )
    }
}

// ── 状态栏 ──
@Composable
private fun StatusLine(
    state: State,
    isPaused: Boolean,
    isDark: Boolean,
    isOverclock: Boolean,
) {
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

    val scrambleAnim = remember { Animatable(1f) }
    var scrambleSeed by remember { mutableIntStateOf(0) }
    LaunchedEffect(isOverclock) {
        if (isOverclock) {
            scrambleAnim.snapTo(0f)
            launch {
                while (scrambleAnim.value < 1f) {
                    scrambleSeed++
                    delay(45)
                }
            }
            scrambleAnim.animateTo(1f, tween(520, easing = LinearEasing))
        } else {
            scrambleAnim.snapTo(1f)
        }
    }

    val label = when {
        isOverclock -> {
            val total = OVER_WORD.length + CLOCK_WORD.length
            val shown = (scrambleAnim.value * total).toInt()
            buildString {
                for (i in 0 until total) {
                    val real = if (i < OVER_WORD.length) OVER_WORD[i]
                    else CLOCK_WORD[i - OVER_WORD.length]
                    if (i < shown) append(real)
                    else append(ScramblePool[(scrambleSeed + i * 3) % ScramblePool.size])
                }
            }
        }
        isPaused -> stringResource(R.string.state_paused)
        state == State.FOCUS -> stringResource(R.string.state_focus)
        state == State.SHORT_BREAK -> stringResource(R.string.state_short_break)
        state == State.LONG_BREAK -> stringResource(R.string.state_long_break)
        else -> stringResource(R.string.state_ready)
    }

    val dotColor = when {
        isOverclock -> OverStart
        isPaused -> Color(0xFFFFB300)
        state == State.FOCUS -> OrangeStart
        state == State.SHORT_BREAK || state == State.LONG_BREAK -> Color(0xFF4CAF50)
        else -> Color(0xFF666666)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .alpha(if (state != State.IDLE || isOverclock) breathAlpha else 1f)
                .background(dotColor)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isOverclock) FontWeight.Medium else FontWeight.Light,
            letterSpacing = 5.sp,
            style = if (isOverclock) TextStyle(
                brush = Brush.linearGradient(listOf(OverStart, OverEnd))
            ) else TextStyle.Default,
            color = if (isOverclock) Color.Unspecified
            else if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline
        )
    }
}

// ── 预设行 ──
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

// ── 仪表盘 ──
@Composable
private fun DashboardRing(
    snap: Snapshot,
    isDark: Boolean,
    presetFocusMin: Int,
    selfCheckRing: Float?,
    selfCheckNum: Float?,
    overclockIntro: Boolean,
    overclockTextAlpha: Float,
    overclockRevealCount: Float,
    overclockGlow: Float,
    overclockScrambleSeed: Int,
    themeStart: Color,
    themeEnd: Color,
) {
    val trackColor = if (isDark) DarkTrack else Color(0xFFEAEAEA)
    val textColor = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val subColor = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    val realProgress = if (snap.plannedMs == 0L) 0f
    else (1f - snap.remainMs.toFloat() / snap.plannedMs.toFloat()).coerceIn(0f, 1f)

    val rawProgress = selfCheckRing ?: realProgress
    val mainProgress = rawProgress.coerceIn(0f, 1f)

    val deepness = ((rawProgress - 1f) / 0.10f).coerceIn(0f, 1f)

    val arcA = lerp(themeStart, DeepOrangeStart, deepness)
    val arcB = lerp(themeEnd, DeepOrangeEnd, deepness)
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

    val reveal = overclockRevealCount.toInt().coerceIn(0, 9)
    val overShown = buildString {
        for (i in OVER_WORD.indices) {
            if (i < reveal) append(OVER_WORD[i])
            else append(ScramblePool[(overclockScrambleSeed + i * 5) % ScramblePool.size])
        }
    }
    val clockShown = buildString {
        for (i in CLOCK_WORD.indices) {
            val idx = i + OVER_WORD.length
            if (idx < reveal) append(CLOCK_WORD[i])
            else append(ScramblePool[(overclockScrambleSeed + idx * 5) % ScramblePool.size])
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

            val glow = overclockGlow.coerceIn(0f, 1f)

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
                val baseColor = if (done) arcA else trackColor
                val tickColor = if (done && glow > 0f) {
                    lerp(baseColor, GoldColor, glow * 0.85f)
                } else baseColor
                val baseW = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                val tickW = baseW * (1f + glow * 0.7f)
                drawLine(
                    color = tickColor,
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = tickW,
                    cap = StrokeCap.Round
                )
            }

            if (mainProgress > 0f && glow > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            GoldColor.copy(alpha = 0.42f * glow),
                            GoldColor.copy(alpha = 0.42f * glow),
                            GoldColor.copy(alpha = 0.42f * glow)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * mainProgress,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth * 3.6f, cap = StrokeCap.Round)
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
            Box(contentAlignment = Alignment.Center, modifier = Modifier.width(180.dp)) {
                Text(
                    text = timeText,
                    fontSize = 62.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = if (overclockIntro) themeStart else textColor,
                    modifier = Modifier.alpha(1f - overclockTextAlpha)
                )

                if (overclockTextAlpha > 0f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.alpha(overclockTextAlpha)
                    ) {
                        Text(
                            text = overShown,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            style = TextStyle(
                                brush = Brush.linearGradient(listOf(OverStart, OverEnd))
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = clockShown,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            style = TextStyle(
                                brush = Brush.linearGradient(listOf(OverStart, OverEnd))
                            )
                        )
                    }
                }
            }
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

// ── 控制按钮（大按钮缩小消散 → 三按钮错峰弹簧弹入）──
@Composable
private fun ControlButtons(
    snap: Snapshot,
    themeStart: Color,
    themeEnd: Color,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    val animatedStart by animateColorAsState(
        targetValue = themeStart,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "ctrlStart"
    )
    val animatedEnd by animateColorAsState(
        targetValue = themeEnd,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "ctrlEnd"
    )

    val isIdle = snap.state == State.IDLE

    AnimatedContent(
        targetState = isIdle,
        transitionSpec = {
            val enter = fadeIn(tween(1))
            val exit = (
                    fadeOut(tween(220, easing = FastOutSlowInEasing)) +
                            scaleOut(
                                targetScale = 0.65f,
                                animationSpec = tween(320, easing = FastOutSlowInEasing)
                            )
                    )
            enter togetherWith exit
        },
        label = "ctrlGroup",
    ) { idle ->
        if (idle) {
            StartButton(animatedStart, animatedEnd, onStart)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StaggerPill(index = 0) {
                    if (snap.isPaused) {
                        PrimaryPill(stringResource(R.string.home_resume), animatedStart, animatedEnd, onResume)
                    } else {
                        GhostPill(stringResource(R.string.home_pause), onPause)
                    }
                }
                StaggerPill(index = 1) {
                    GhostPill(stringResource(R.string.home_skip), onSkip)
                }
                StaggerPill(index = 2) {
                    PrimaryPill(stringResource(R.string.home_finish), animatedStart, animatedEnd, onFinish)
                }
            }
        }
    }
}

// ── 错峰弹簧弹入 ──
@Composable
private fun StaggerPill(
    index: Int,
    content: @Composable () -> Unit,
) {
    val scale = remember { Animatable(0.55f) }
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(22f) }
    val rotation = remember {
        Animatable(if (index == 0) -6f else if (index == 2) 6f else 0f)
    }

    LaunchedEffect(Unit) {
        delay(index * 60L)
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
        launch { alpha.animateTo(1f, tween(200, easing = FastOutSlowInEasing)) }
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
        rotation.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
            translationY = offsetY.value
            rotationZ = rotation.value
        }
    ) {
        content()
    }
}

// ── 大按钮 ──
@Composable
private fun StartButton(
    themeStart: Color,
    themeEnd: Color,
    onClick: () -> Unit,
) {
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
                .background(Brush.horizontalGradient(listOf(themeStart, themeEnd)))
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
}

@Composable
private fun PrimaryPill(
    label: String,
    themeStart: Color,
    themeEnd: Color,
    onClick: () -> Unit
) {
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
                .background(Brush.horizontalGradient(listOf(themeStart, themeEnd)))
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
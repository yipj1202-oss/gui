package com.gui.tomato.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.entities.Task
import com.gui.tomato.ui.components.StaggerIn
import com.gui.tomato.util.Haptic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)
private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

private val ScramblePool = "!@#$%&*+-=?<>[]{}|/\\~^;:アイウエオカキ".toList()

private val CharWidthDp = 22.dp

private const val CHARGE_MS = 700
private const val TAP_MS = 180

private class JobHolder(var job: Job? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var activeList by remember { mutableStateOf<List<Task>>(emptyList()) }
    var doneList by remember { mutableStateOf<List<Task>>(emptyList()) }

    LaunchedEffect(Unit) {
        app.db.taskDao().activeFlow().collect { activeList = it }
    }
    LaunchedEffect(Unit) {
        app.db.taskDao().doneFlow().collect { doneList = it }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background
    val cardColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline

    val vibrateOn = app.prefs.vibrate

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    StaggerIn(index = 0, startOffsetY = 14.dp) {
                        Text(
                            stringResource(R.string.nav_tasks),
                            color = textMain,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    StaggerIn(index = 1, startOffsetY = 14.dp) {
                        IconButton(onClick = { nav.navigate("settings") }) {
                            Text(
                                text = "晷",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Thin,
                                letterSpacing = 1.sp,
                                style = TextStyle(
                                    brush = Brush.linearGradient(listOf(OrangeStart, OrangeEnd))
                                )
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            StaggerIn(index = 9, startOffsetY = 20.dp, startScale = 0.85f) {
                ExpandableAddFab(
                    onClick = { nav.navigate("taskEdit/0") },
                    fullText = stringResource(R.string.task_new),
                )
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            if (activeList.isEmpty() && doneList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    StaggerIn(index = 2) {
                        Text(
                            stringResource(R.string.task_empty_hint),
                            color = textSub,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 8.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeList.isNotEmpty()) {
                        item {
                            StaggerIn(index = 2) {
                                Text(
                                    stringResource(R.string.task_section_active),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = textSub,
                                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                                )
                            }
                        }
                        itemsIndexed(activeList, key = { _, t -> t.id }) { idx, t ->
                            StaggerIn(index = 3 + idx, modifier = Modifier.fillMaxWidth()) {
                                SwipeToRevealDelete(
                                    deleteLabel = stringResource(R.string.task_delete_desc),
                                    vibrate = vibrateOn,
                                    chargeEnabled = true,
                                    onClickContent = { nav.navigate("taskDetail/" + t.id) },
                                    onChargeComplete = {
                                        app.pendingStartTaskId = t.id
                                        nav.navigate("tasks") {
                                            popUpTo("home") { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    },
                                    onDelete = {
                                        kotlinx.coroutines.runBlocking {
                                            app.db.taskDao().delete(t.id)
                                        }
                                    }
                                ) {
                                    TaskCard(
                                        task = t,
                                        cardColor = cardColor,
                                        textMain = textMain,
                                        textSub = textSub,
                                        onToggle = {
                                            kotlinx.coroutines.runBlocking {
                                                app.db.taskDao().update(
                                                    t.copy(completed = true, completedAt = System.currentTimeMillis())
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (doneList.isNotEmpty()) {
                        item {
                            StaggerIn(index = 3 + activeList.size) {
                                Text(
                                    stringResource(R.string.task_section_done),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = textSub,
                                    modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                                )
                            }
                        }
                        itemsIndexed(doneList, key = { _, t -> t.id }) { idx, t ->
                            StaggerIn(
                                index = 4 + activeList.size + idx,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SwipeToRevealDelete(
                                    deleteLabel = stringResource(R.string.task_delete_desc),
                                    vibrate = vibrateOn,
                                    chargeEnabled = false,
                                    onClickContent = { nav.navigate("taskDetail/" + t.id) },
                                    onChargeComplete = {},
                                    onDelete = {
                                        kotlinx.coroutines.runBlocking {
                                            app.db.taskDao().delete(t.id)
                                        }
                                    }
                                ) {
                                    TaskCard(
                                        task = t,
                                        cardColor = cardColor,
                                        textMain = textMain,
                                        textSub = textSub,
                                        onToggle = {
                                            kotlinx.coroutines.runBlocking {
                                                app.db.taskDao().update(
                                                    t.copy(completed = false, completedAt = null)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── 左滑掀盖 + 长按蓄力 ──
@Composable
private fun SwipeToRevealDelete(
    deleteLabel: String,
    vibrate: Boolean,
    chargeEnabled: Boolean,
    onClickContent: () -> Unit,
    onChargeComplete: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var cardHeightPx by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cardWidthPx = with(density) { maxWidth.toPx() }
        val thirdPx = cardWidthPx / 3f
        val overlapPx = with(density) { 16.dp.toPx() }
        val deleteBarWidthDp = with(density) { (thirdPx + overlapPx).toDp() }
        val thirdDp = with(density) { thirdPx.toDp() }
        val cardHeightDp = with(density) { cardHeightPx.toDp() }
        val openThresholdPx = thirdPx * 0.40f

        val offsetX = remember { Animatable(0f) }
        val killProgress = remember { Animatable(0f) }
        val chargeAnim = remember { Animatable(0f) }
        var isOpen by remember { mutableStateOf(false) }
        var isDeleting by remember { mutableStateOf(false) }

        val chargeJobRef = remember { JobHolder() }
        val chargedRef = remember { mutableStateOf(false) }

        val onChargeCompleteRef = rememberUpdatedState(onChargeComplete)
        val vibrateRef = rememberUpdatedState(vibrate)

        fun open() {
            isOpen = true
            scope.launch {
                offsetX.animateTo(
                    -thirdPx,
                    spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessMedium)
                )
            }
        }

        fun close() {
            isOpen = false
            scope.launch {
                offsetX.animateTo(
                    0f,
                    spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMedium)
                )
            }
        }

        // 湮灭参数
        val p = killProgress.value
        val scaleX = when {
            p < 0.55f -> 1f - (p / 0.55f) * 0.95f
            p < 0.85f -> 0.05f
            else -> 0.05f * (1f - (p - 0.85f) / 0.15f)
        }
        val scaleY = when {
            p < 0.55f -> 1f + (p / 0.55f) * 0.06f
            p < 0.85f -> 1.06f * (1f - (p - 0.55f) / 0.30f)
            else -> 0f
        }
        val alpha = if (p < 0.85f) 1f else (1f - (p - 0.85f) / 0.15f).coerceIn(0f, 1f)
        val glow = if (p > 0.80f) ((p - 0.80f) / 0.20f).coerceIn(0f, 1f) else 0f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                    this.alpha = alpha
                }
        ) {
            // ── 底层：删除条 ──
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(deleteBarWidthDp)
                    .height(cardHeightDp)
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                    .background(
                        Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd))
                    )
                    .clickable(enabled = isOpen && !isDeleting) {
                        if (vibrate) Haptic.tap(ctx)
                        isDeleting = true
                        scope.launch {
                            killProgress.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
                            onDelete()
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(thirdDp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = deleteLabel,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = deleteLabel,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 3.sp
                        )
                    }
                }
                if (glow > 0f) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = glow * 0.85f),
                                        Color.White.copy(alpha = glow * 0.55f)
                                    )
                                )
                            )
                    )
                }
            }

            // ── 前景卡片 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { cardHeightPx = it.height }
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput(thirdPx, isOpen, isDeleting, chargeEnabled) {
                        if (isDeleting) return@pointerInput
                        awaitEachGesture {
                            // requireUnconsumed=true：内层 Checkbox 消费了就跳过
                            val down = awaitFirstDown(requireUnconsumed = true)
                            val downTimeMs = down.uptimeMillis

                            if (isOpen) {
                                val up = waitForUpOrCancellation()
                                if (up != null) {
                                    if (vibrate) Haptic.tap(ctx)
                                    close()
                                }
                                return@awaitEachGesture
                            }

                            // 启动蓄力
                            if (chargeEnabled) {
                                chargedRef.value = false
                                chargeJobRef.job?.cancel()
                                chargeJobRef.job = scope.launch {
                                    try {
                                        chargeAnim.snapTo(0f)
                                        chargeAnim.animateTo(
                                            targetValue = 1f,
                                            animationSpec = tween(CHARGE_MS, easing = LinearEasing)
                                        )
                                        chargedRef.value = true
                                        if (vibrateRef.value) Haptic.tap(ctx)
                                    } catch (_: CancellationException) {
                                    }
                                }
                            }

                            var isDragging = false
                            var upTimeMs = downTimeMs

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) {
                                    upTimeMs = change.uptimeMillis
                                    break
                                }

                                val dx = change.position.x - change.previousPosition.x

                                if (!isDragging && abs(dx) > 6f) {
                                    isDragging = true
                                    chargeJobRef.job?.cancel()
                                    chargedRef.value = false
                                    scope.launch {
                                        chargeAnim.animateTo(0f, tween(120))
                                    }
                                }

                                if (isDragging) {
                                    val nv = (offsetX.value + dx).coerceIn(-thirdPx, 0f)
                                    scope.launch { offsetX.snapTo(nv) }
                                    change.consume()
                                }
                            }

                            val pressMs = upTimeMs - downTimeMs
                            val wasCharged = chargedRef.value

                            if (!wasCharged) {
                                chargeJobRef.job?.cancel()
                                if (chargeAnim.value > 0.001f) {
                                    scope.launch {
                                        try {
                                            chargeAnim.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
                                        } catch (_: CancellationException) {
                                        }
                                    }
                                }
                            }

                            if (wasCharged) {
                                onChargeCompleteRef.value()
                                return@awaitEachGesture
                            }

                            when {
                                isDragging -> {
                                    if (-offsetX.value >= openThresholdPx) open() else close()
                                }
                                pressMs < TAP_MS -> {
                                    // 短按 → 进详情
                                    onClickContent()
                                }
                                // 长按但未蓄满 → 什么都不做
                            }
                        }
                    }
            ) {
                content()

                // 进度环：matchParentSize 贴合卡片实际大小
                ChargeRing(
                    progress = chargeAnim.value,
                    cornerRadius = 16.dp,
                    strokeWidth = 3.dp,
                    color = OrangeStart,
                )
            }
        }
    }
}

// ── 边缘进度环：用 PathMeasure 沿圆角矩形取部分路径绘制（BoxScope）──
@Composable
private fun BoxScope.ChargeRing(
    progress: Float,
    cornerRadius: Dp,
    strokeWidth: Dp,
    color: Color,
) {
    if (progress <= 0.001f) return
    Canvas(modifier = Modifier.matchParentSize()) {
        val strokePx = strokeWidth.toPx()
        val r = cornerRadius.toPx()
        val inset = strokePx / 2f

        val rect = Rect(
            inset, inset,
            size.width - inset, size.height - inset
        )
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = rect,
                    cornerRadius = CornerRadius(r, r)
                )
            )
        }

        val pm = PathMeasure()
        pm.setPath(path, false)
        val total = pm.length
        if (total <= 0f) return@Canvas

        val frac = progress.coerceIn(0f, 1f)
        val seg = Path()
        val ok = pm.getSegment(0f, total * frac, seg, true)
        if (!ok) return@Canvas

        drawPath(
            path = seg,
            color = color.copy(alpha = (0.55f + frac * 0.45f).coerceIn(0f, 1f)),
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

// ── 展开式添加 FAB ──
@Composable
private fun ExpandableAddFab(
    onClick: () -> Unit,
    fullText: String,
) {
    var shownChars by remember { mutableIntStateOf(0) }
    var scrambling by remember { mutableStateOf(false) }
    var scrambleSeed by remember { mutableIntStateOf(0) }

    LaunchedEffect(fullText) {
        shownChars = 0
        scrambling = false

        while (true) {
            scrambleSeed++
            delay(40)
        }
    }

    LaunchedEffect(fullText) {
        delay(700)
        for (i in fullText.indices) {
            scrambling = true
            delay(320)
            shownChars = i + 1
            scrambling = false
            delay(220)
        }
    }

    val visibleChars = if (scrambling) shownChars + 1 else shownChars
    val targetWidth = 56.dp + CharWidthDp * visibleChars
    val widthDp by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "fabWidth",
    )

    val displayText = buildString {
        append(fullText.take(shownChars))
        if (scrambling && shownChars < fullText.length) {
            val idx = ((scrambleSeed * 7 + shownChars * 3) % ScramblePool.size + ScramblePool.size) % ScramblePool.size
            append(ScramblePool[idx])
        }
    }

    Box(
        modifier = Modifier
            .height(56.dp)
            .width(widthDp)
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd)))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 17.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = fullText,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            if (displayText.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = displayText,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

// ── 任务卡片 ──
@Composable
private fun TaskCard(
    task: Task,
    cardColor: Color,
    textMain: Color,
    textSub: Color,
    onToggle: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = cardColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 自定义 Checkbox：显式消费 down，避免触发卡片点击
            TapCheckbox(
                checked = task.completed,
                onToggle = onToggle,
                textSub = textSub,
            )
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.completed) textSub else textMain,
                    fontWeight = FontWeight.Medium
                )
                if (task.note.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        task.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSub,
                        maxLines = 2
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
        }
    }
}

// ── 自定义 Checkbox ──
@Composable
private fun TapCheckbox(
    checked: Boolean,
    onToggle: () -> Unit,
    textSub: Color,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = true)
                    down.consume() // 关键：消费 down，外层 pointerInput 收不到
                    val up = waitForUpOrCancellation()
                    if (up != null) {
                        up.consume()
                        onToggle()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (checked) OrangeStart else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (checked) OrangeStart else textSub.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(5.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
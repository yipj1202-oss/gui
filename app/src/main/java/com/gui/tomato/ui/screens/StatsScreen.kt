package com.gui.tomato.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.HourStatRow
import com.gui.tomato.data.TaskStatRow
import com.gui.tomato.ui.components.StaggerIn
import com.gui.tomato.util.Haptic
import kotlin.math.roundToInt

private val DarkBg = Color(0xFF0D0D0D)
private val DarkCard = Color(0xFF1A1A1A)
private val DarkSheetRow = Color(0xFF141414)
private val DarkTrack = Color(0xFF2A2A2A)
private val DarkDivider = Color(0xFF262626)
private val DarkTextSub = Color(0xFF8A8A8A)
private val DarkTextMain = Color(0xFFFFFFFF)
private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

// 折线趋势色（想换别的改这里）
private val TrendColor = Color(0xFFFFD08A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var range by remember { mutableIntStateOf(0) }
    var rows by remember { mutableStateOf<List<TaskStatRow>>(emptyList()) }
    var hours by remember { mutableStateOf<List<HourStatRow>>(emptyList()) }
    var cursorHour by remember { mutableStateOf<Int?>(null) }

    // 删除统计：确认对话框目标 + 重载触发器
    var deleteTarget by remember { mutableStateOf<TaskStatRow?>(null) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(range, reloadTrigger) {
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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) DarkBg else MaterialTheme.colorScheme.background
    val textMain = if (isDark) DarkTextMain else MaterialTheme.colorScheme.onBackground
    val textSub = if (isDark) DarkTextSub else MaterialTheme.colorScheme.outline
    val trackColor = if (isDark) DarkTrack else Color(0xFFEAEAEA)
    val gridColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFE0E0E0)
    val cardBg = if (isDark) DarkSheetRow else Color(0xFFF5F1EA)
    val dividerColor = if (isDark) DarkDivider else Color(0xFFE5E5E5)

    val primary = MaterialTheme.colorScheme.primary
    val secondary = TrendColor

    val maxTask = rows.maxOfOrNull { it.focusMin }?.takeIf { it > 0 } ?: 1

    val labelToday = stringResource(R.string.stats_today)
    val labelWeek = stringResource(R.string.stats_week)
    val labelMonth = stringResource(R.string.stats_month)
    val labelHourly = stringResource(R.string.stats_hourly_distribution)
    val labelNoHourly = stringResource(R.string.stats_no_hourly_data)
    val labelByTask = stringResource(R.string.stats_task_distribution)
    val labelNoFocus = stringResource(R.string.stats_no_focus_data)
    val labelCursor = stringResource(R.string.stats_cursor)
    val labelCursorFocus = stringResource(R.string.stats_cursor_focus)
    val labelLegendFocus = stringResource(R.string.stats_legend_focus_min)
    val labelLegendTrend = stringResource(R.string.stats_legend_trend)
    val hourSuffix = stringResource(R.string.stats_hour_suffix)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    StaggerIn(index = 0, startOffsetY = 14.dp) {
                        Text(
                            stringResource(R.string.nav_stats),
                            color = textMain,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── 范围选择 ──
            StaggerIn(index = 1, startOffsetY = 14.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val labels = listOf(labelToday, labelWeek, labelMonth)
                    labels.forEachIndexed { i, l ->
                        FilterChip(
                            selected = range == i,
                            onClick = { range = i },
                            label = { Text(l) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primary.copy(alpha = 0.18f),
                                selectedLabelColor = primary,
                                labelColor = textSub
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── 时段分布 ──
            StaggerIn(index = 2) {
                Text(labelHourly, style = MaterialTheme.typography.titleMedium, color = textMain)
            }
            Spacer(Modifier.height(12.dp))
            if (hours.isEmpty() || hours.all { it.focusMin == 0 }) {
                StaggerIn(index = 3) {
                    EmptyHint(labelNoHourly, textSub)
                }
            } else {
                StaggerIn(
                    index = 3,
                    startOffsetX = 40.dp,
                    startOffsetY = 20.dp,
                ) {
                    HourBarChart(
                        hours = hours,
                        barColor = primary,
                        lineColor = secondary,
                        gridColor = gridColor,
                        labelColor = textSub,
                        labelStyle = MaterialTheme.typography.labelSmall,
                        cursorHour = cursorHour,
                        onCursorChange = { cursorHour = it },
                        textMain = textMain,
                        textSub = textSub,
                        labelCursor = labelCursor,
                        labelCursorFocus = labelCursorFocus,
                        hourSuffix = hourSuffix,
                    )
                }
                Spacer(Modifier.height(12.dp))
                StaggerIn(index = 4) {
                    Legend(primary, secondary, textSub, labelLegendFocus, labelLegendTrend)
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── 按任务分布 ──
            StaggerIn(index = 5) {
                Text(labelByTask, style = MaterialTheme.typography.titleMedium, color = textMain)
            }
            Spacer(Modifier.height(4.dp))
            StaggerIn(index = 6) {
                Text(
                    text = "长按任一行可删除该任务的全部专注记录",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    color = textSub.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(12.dp))
            if (rows.isEmpty() || rows.all { it.focusMin == 0 }) {
                StaggerIn(index = 7) {
                    EmptyHint(labelNoFocus, textSub)
                }
            } else {
                rows.take(10).forEachIndexed { idx, r ->
                    StaggerIn(
                        index = 7 + idx,
                        modifier = Modifier.fillMaxWidth(),
                        startOffsetX = 32.dp,
                    ) {
                        TaskBarRow(
                            r = r,
                            maxTask = maxTask,
                            barColor = primary,
                            trackColor = trackColor,
                            textMain = textMain,
                            textSub = textSub,
                            cardBg = cardBg,
                            dividerColor = dividerColor,
                            onLongPress = {
                                if (app.prefs.vibrate) Haptic.tap(ctx)
                                deleteTarget = r
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── 删除确认对话框 ──
    val target = deleteTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = if (isDark) DarkCard else MaterialTheme.colorScheme.surface,
            titleContentColor = textMain,
            textContentColor = textSub,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "删除统计数据",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 4.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "确定删除「${target.taskName}」的全部专注记录？",
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "共 ${target.focusMin} 分钟 · ${target.pomodoros} 个番茄",
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = textSub
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "此操作不可恢复",
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        color = OrangeStart
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd))
                        )
                        .clickable {
                            if (app.prefs.vibrate) Haptic.tap(ctx)
                            app.deleteTaskStats(target.taskId)
                            deleteTarget = null
                            reloadTrigger++
                        }
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                ) {
                    Text(
                        "删除",
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
                        .border(
                            1.dp,
                            textSub.copy(alpha = 0.3f),
                            RoundedCornerShape(999.dp)
                        )
                        .clickable { deleteTarget = null }
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                ) {
                    Text(
                        "取消",
                        color = textSub,
                        fontSize = 13.sp,
                        letterSpacing = 3.sp
                    )
                }
            }
        )
    }
}

@Composable
private fun EmptyHint(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun TaskBarRow(
    r: TaskStatRow,
    maxTask: Int,
    barColor: Color,
    trackColor: Color,
    textMain: Color,
    textSub: Color,
    cardBg: Color,
    dividerColor: Color,
    onLongPress: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, dividerColor, RoundedCornerShape(12.dp))
            .pointerInput(r.taskId) {
                detectTapGestures(onLongPress = { onLongPress() })
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            r.taskName,
            style = MaterialTheme.typography.bodyMedium,
            color = textMain,
            modifier = Modifier.width(84.dp),
            maxLines = 1
        )
        Canvas(
            modifier = Modifier
                .height(16.dp)
                .weight(1f)
        ) {
            val h = 12.dp.toPx()
            val top = (size.height - h) / 2f
            val r2 = h / 2f
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, top),
                size = Size(size.width, h),
                cornerRadius = CornerRadius(r2, r2)
            )
            val fillW = size.width * (r.focusMin.toFloat() / maxTask).coerceIn(0.02f, 1f)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, top),
                size = Size(fillW, h),
                cornerRadius = CornerRadius(r2, r2)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.stats_task_row_format, r.focusMin, r.pomodoros),
            style = MaterialTheme.typography.bodySmall,
            color = textSub,
            modifier = Modifier.width(78.dp)
        )
    }
}

@Composable
private fun HourBarChart(
    hours: List<HourStatRow>,
    barColor: Color,
    lineColor: Color,
    gridColor: Color,
    labelColor: Color,
    labelStyle: TextStyle,
    cursorHour: Int?,
    onCursorChange: (Int?) -> Unit,
    textMain: Color,
    textSub: Color,
    labelCursor: String,
    labelCursorFocus: String,
    hourSuffix: String,
) {
    val textMeasurer = rememberTextMeasurer()
    val hourMap = remember(hours) { hours.associateBy { it.hour } }

    val rawMax = hours.maxOfOrNull { it.focusMin } ?: 1
    val displayMax = when {
        rawMax <= 5 -> 5
        rawMax <= 10 -> 10
        rawMax <= 15 -> 15
        rawMax <= 30 -> 30
        rawMax <= 60 -> 60
        else -> ((rawMax / 30) + 1) * 30
    }

    val cursorMin: Int = cursorHour?.let { hourMap[it]?.focusMin ?: 0 } ?: 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = labelCursor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.sp,
                    color = textSub
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = cursorHour?.let { "%02d".format(it) } ?: "--",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 2.sp,
                    color = if (cursorHour != null) textMain else textSub.copy(alpha = 0.35f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = labelCursorFocus,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.sp,
                    color = textSub
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = cursorHour?.let { cursorMin.toString() } ?: "--",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 2.sp,
                    color = when {
                        cursorHour == null -> textSub.copy(alpha = 0.35f)
                        cursorMin > 0 -> barColor
                        else -> textSub
                    }
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .pointerInput(hours) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val lP = 40.dp.toPx()
                            val rP = 8.dp.toPx()
                            val cW = size.width - lP - rP
                            val sW = cW / 24f
                            val h = ((offset.x - lP) / sW).roundToInt().coerceIn(0, 23)
                            onCursorChange(h)
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val lP = 40.dp.toPx()
                            val rP = 8.dp.toPx()
                            val cW = size.width - lP - rP
                            val sW = cW / 24f
                            val h = ((change.position.x - lP) / sW).roundToInt().coerceIn(0, 23)
                            onCursorChange(h)
                        },
                    )
                }
        ) {
            val leftPad = 40.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = 22.dp.toPx()
            val bottomPad = 26.dp.toPx()

            val chartW = size.width - leftPad - rightPad
            val chartH = size.height - topPad - bottomPad
            val slotW = chartW / 24f
            val barW = (slotW * 0.55f).coerceAtLeast(3.dp.toPx())
            val cornerR = (barW / 2f).coerceAtMost(6.dp.toPx())

            val ySteps = 4
            for (i in 0..ySteps) {
                val y = topPad + chartH * (1f - i.toFloat() / ySteps)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + chartW, y),
                    strokeWidth = 1.dp.toPx()
                )
                val value = (displayMax * i / ySteps)
                val layout = textMeasurer.measure(value.toString(), labelStyle)
                drawText(
                    textLayoutResult = layout,
                    color = labelColor,
                    topLeft = Offset(
                        leftPad - layout.size.width - 4.dp.toPx(),
                        y - layout.size.height / 2f
                    )
                )
            }

            for (i in 0 until 24) {
                val min = hourMap[i]?.focusMin ?: 0
                if (min <= 0) continue

                val left = leftPad + i * slotW + (slotW - barW) / 2f
                val barH = (min.toFloat() / displayMax) * chartH
                val top = topPad + chartH - barH
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(cornerR, cornerR)
                )

                val layout = textMeasurer.measure(
                    min.toString(),
                    labelStyle.copy(fontSize = 9.sp)
                )
                drawText(
                    textLayoutResult = layout,
                    color = labelColor,
                    topLeft = Offset(
                        left + (barW - layout.size.width) / 2f,
                        (top - layout.size.height - 2.dp.toPx()).coerceAtLeast(0f)
                    )
                )
            }

            val linePoints = mutableListOf<Offset>()
            for (i in 0 until 24) {
                val min = hourMap[i]?.focusMin ?: 0
                val cx = leftPad + i * slotW + slotW / 2f
                val cy = if (min > 0) {
                    topPad + chartH - (min.toFloat() / displayMax) * chartH
                } else {
                    topPad + chartH
                }
                linePoints.add(Offset(cx, cy))
            }

            if (linePoints.size >= 2) {
                val path = Path().apply {
                    moveTo(linePoints[0].x, linePoints[0].y)
                    for (k in 1 until linePoints.size) {
                        lineTo(linePoints[k].x, linePoints[k].y)
                    }
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 2.dp.toPx())
                )
                for (i in 0 until 24) {
                    val min = hourMap[i]?.focusMin ?: 0
                    if (min > 0) {
                        val p = linePoints[i]
                        drawCircle(color = lineColor, radius = 3.dp.toPx(), center = p)
                        drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = p)
                    }
                }
            }

            listOf(0, 6, 12, 18, 23).forEach { hour ->
                val x = leftPad + hour * slotW + slotW / 2f
                val layout = textMeasurer.measure("$hour$hourSuffix", labelStyle)
                drawText(
                    textLayoutResult = layout,
                    color = labelColor,
                    topLeft = Offset(
                        x - layout.size.width / 2f,
                        topPad + chartH + 6.dp.toPx()
                    )
                )
            }

            cursorHour?.let { ch ->
                val cx = leftPad + ch * slotW + slotW / 2f
                val cMin = hourMap[ch]?.focusMin ?: 0

                drawLine(
                    color = barColor.copy(alpha = 0.55f),
                    start = Offset(cx, topPad),
                    end = Offset(cx, topPad + chartH),
                    strokeWidth = 1.5.dp.toPx()
                )

                val cy = if (cMin > 0) {
                    topPad + chartH - (cMin.toFloat() / displayMax) * chartH
                } else {
                    topPad + chartH
                }
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = barColor, radius = 4.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = Offset(cx, cy))
            }
        }
    }
}

@Composable
private fun Legend(
    barColor: Color,
    lineColor: Color,
    textColor: Color,
    labelFocusMin: String,
    labelTrend: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawRoundRect(
                color = barColor,
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(labelFocusMin, style = MaterialTheme.typography.labelSmall, color = textColor)
        Spacer(Modifier.width(20.dp))
        Canvas(modifier = Modifier.size(16.dp)) {
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
        }
        Spacer(Modifier.width(6.dp))
        Text(labelTrend, style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}
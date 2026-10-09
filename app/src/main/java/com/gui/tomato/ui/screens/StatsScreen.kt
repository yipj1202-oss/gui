package com.gui.tomato.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.data.HourStatRow
import com.gui.tomato.data.TaskStatRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    var range by remember { mutableIntStateOf(0) }
    var rows by remember { mutableStateOf<List<TaskStatRow>>(emptyList()) }
    var hours by remember { mutableStateOf<List<HourStatRow>>(emptyList()) }

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

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val gridColor = outline.copy(alpha = 0.18f)
    val trackColor = outline.copy(alpha = 0.15f)

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.stats_title)) }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val ranges = listOf(
                    stringResource(R.string.stats_today),
                    stringResource(R.string.stats_week),
                    stringResource(R.string.stats_month)
                )
                ranges.forEachIndexed { i, l ->
                    FilterChip(
                        selected = range == i,
                        onClick = { range = i },
                        label = { Text(l) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.stats_task_distribution), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            if (rows.isEmpty() || rows.all { it.focusMin == 0 }) {
                EmptyHint(stringResource(R.string.stats_no_focus_data))
            } else {
                rows.take(10).forEach { r ->
                    TaskBarRow(r, maxTask, primary, trackColor, onSurface)
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.stats_hourly_distribution), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            if (hours.isEmpty() || hours.all { it.focusMin == 0 }) {
                EmptyHint(stringResource(R.string.stats_no_hourly_data))
            } else {
                HourBarChart(
                    hours = hours,
                    barColor = primary,
                    lineColor = secondary,
                    gridColor = gridColor,
                    labelColor = onSurface,
                    labelStyle = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(16.dp))
            Legend(primary, secondary)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun TaskBarRow(
    r: TaskStatRow,
    maxTask: Int,
    barColor: Color,
    trackColor: Color,
    labelColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            r.taskName,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor,
            modifier = Modifier.width(90.dp),
            maxLines = 1
        )
        Canvas(
            modifier = Modifier
                .height(18.dp)
                .weight(1f)
        ) {
            val h = 14.dp.toPx()
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
            color = labelColor,
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
    labelStyle: TextStyle
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

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
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

        val linePoints = mutableListOf<Offset>()
        for (i in 0 until 24) {
            val min = hourMap[i]?.focusMin ?: 0
            val left = leftPad + i * slotW + (slotW - barW) / 2f

            if (min > 0) {
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

                linePoints.add(Offset(left + barW / 2f, top))
            }
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
            linePoints.forEach { p ->
                drawCircle(color = lineColor, radius = 3.dp.toPx(), center = p)
                drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = p)
            }
        }

        listOf(0, 6, 12, 18, 23).forEach { hour ->
            val x = leftPad + hour * slotW + slotW / 2f
            val layout = textMeasurer.measure("${hour}时", labelStyle)
            drawText(
                textLayoutResult = layout,
                color = labelColor,
                topLeft = Offset(
                    x - layout.size.width / 2f,
                    topPad + chartH + 6.dp.toPx()
                )
            )
        }
    }
}

@Composable
private fun Legend(barColor: Color, lineColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(0.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = barColor,
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.stats_legend_focus_min), style = MaterialTheme.typography.labelSmall)
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
        Text(stringResource(R.string.stats_legend_trend), style = MaterialTheme.typography.labelSmall)
    }
}

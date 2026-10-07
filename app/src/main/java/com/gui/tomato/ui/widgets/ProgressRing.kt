package com.gui.tomato.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ProgressRing(progress: Float, sizeDp: Int = 200, label: String = "") {
    val primary: Color = MaterialTheme.colorScheme.primary
    val track: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    Box(Modifier.size(sizeDp.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(sizeDp.dp)) {
            val d = size.width
            val strokeW = 12f
            val radius = (d - strokeW) / 2f
            val cx = d / 2f
            val cy = d / 2f
            drawCircle(
                color = track,
                radius = radius,
                style = Stroke(strokeW, cap = StrokeCap.Round),
                center = androidx.compose.ui.geometry.Offset(cx, cy)
            )
            val arcSize = androidx.compose.ui.geometry.Size(d - strokeW, d - strokeW)
            val topLeft = androidx.compose.ui.geometry.Offset(strokeW / 2f, strokeW / 2f)
            drawArc(
                color = primary,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(strokeW, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }
        Text(label, style = MaterialTheme.typography.headlineMedium)
    }
}
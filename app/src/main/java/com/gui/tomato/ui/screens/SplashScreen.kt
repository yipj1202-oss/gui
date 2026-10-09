package com.gui.tomato.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val SplashBg = Color(0xFF0D0D0D)
private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

@Composable
fun SplashScreen() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        visible = true
    }

    // 三个呼吸点
    val infinite = rememberInfiniteTransition(label = "dots")
    val dotAlpha by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(if (visible) 1f else 0f)
        ) {
            // 大「晷」字，橙色渐变
            Text(
                text = "晷",
                fontSize = 96.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 4.sp,
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(OrangeStart, OrangeEnd)
                    )
                )
            )

            Spacer(Modifier.height(24.dp))

            // 细横线
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.25f))
            )

            Spacer(Modifier.height(24.dp))

            // slogan，大字间距，白色 85%
            Text(
                text = "以 晷 量 时",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "以 行 成 事",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        // 底部三个呼吸点
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { i ->
                val a = if (i == 0) dotAlpha else if (i == 1) (dotAlpha * 0.7f) else (dotAlpha * 0.4f)
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(OrangeStart.copy(alpha = a))
                )
            }
        }
    }
}
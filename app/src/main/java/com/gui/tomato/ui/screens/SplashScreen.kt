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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gui.tomato.GuiApp
import com.gui.tomato.util.Haptic
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private val SplashBg = Color(0xFF0D0D0D)
private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

private val ScramblePool = "!@#$%&*+-=?<>[]{}|/\\~^;:アイウエオカキ".toList()

private const val SLOGAN1 = "以晷量时"
private const val SLOGAN2 = "以行成事"

@Composable
fun SplashScreen() {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as GuiApp
    val hapticsEnabled = app.prefs.vibrate

    // 晷字
    val titleAlpha = remember { Animatable(0f) }
    val titleScale = remember { Animatable(1.15f) }
    val titleShake = remember { Animatable(0f) }
    // 横线 / slogan
    val lineAlpha = remember { Animatable(0f) }
    val sloganFade = remember { Animatable(1f) }

    // slogan 乱码状态
    var s1Shown by remember { mutableIntStateOf(0) }
    var s1Scrambling by remember { mutableStateOf(false) }
    var s2Shown by remember { mutableIntStateOf(0) }
    var s2Scrambling by remember { mutableStateOf(false) }
    var scrambleSeed by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            scrambleSeed++
            delay(38)
        }
    }

    // ── 主时间线 ──
    LaunchedEffect(Unit) {
        delay(180)

        // 1) 晷字浮现（不振动）
        launch { titleAlpha.animateTo(1f, tween(320, easing = FastOutSlowInEasing)) }
        titleScale.animateTo(0.98f, tween(400, easing = FastOutSlowInEasing))

        // 2) 横线浮现（晷字保持显示）
        delay(280)
        lineAlpha.animateTo(1f, tween(140, easing = FastOutSlowInEasing))
        delay(60)

        // 3) slogan1 逐字生成（每字伴随一次轻震）
        for (i in SLOGAN1.indices) {
            s1Scrambling = true
            delay(50)
            s1Shown = i + 1
            s1Scrambling = false
            if (hapticsEnabled) Haptic.tap(ctx)
            delay(20)
        }
        delay(40)

        // 4) slogan2 逐字生成（每字伴随一次轻震）
        for (i in SLOGAN2.indices) {
            s2Scrambling = true
            delay(50)
            s2Shown = i + 1
            s2Scrambling = false
            if (hapticsEnabled) Haptic.tap(ctx)
            delay(20)
        }

        // 5) 全部合并，停留
        delay(420)

        // 6) 晷字一震 + 手机强震
        if (hapticsEnabled)         if (hapticsEnabled) Haptic.strongTap(ctx, 240)
        titleShake.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 260
                0f at 0
                1f at 90
                0f at 260
            }
        )

        // 7) 晷字 / 横线 / slogan 同步淡出
        delay(120)
        launch { titleAlpha.animateTo(0f, tween(320, easing = FastOutSlowInEasing)) }
        launch { sloganFade.animateTo(0f, tween(320, easing = FastOutSlowInEasing)) }
        lineAlpha.animateTo(0f, tween(320, easing = FastOutSlowInEasing))
    }

    // 底部呼吸点
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

    val shakeAmt = sin((titleShake.value * PI).toFloat())
    val shakeScale = 1f + 0.06f * shakeAmt

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "晷",
                fontSize = 120.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 4.sp,
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(OrangeStart, OrangeEnd)
                    )
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    val s = titleScale.value * shakeScale
                    scaleX = s
                    scaleY = s
                }
            )

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(1.dp)
                    .graphicsLayer { alpha = lineAlpha.value }
                    .background(Color.White.copy(alpha = 0.28f))
            )

            Spacer(Modifier.height(26.dp))

            SloganLine(
                text = SLOGAN1,
                shownChars = s1Shown,
                scrambling = s1Scrambling,
                scrambleSeed = scrambleSeed,
                fade = sloganFade.value,
            )

            Spacer(Modifier.height(12.dp))

            SloganLine(
                text = SLOGAN2,
                shownChars = s2Shown,
                scrambling = s2Scrambling,
                scrambleSeed = scrambleSeed,
                fade = sloganFade.value,
            )
        }

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

@Composable
private fun SloganLine(
    text: String,
    shownChars: Int,
    scrambling: Boolean,
    scrambleSeed: Int,
    fade: Float,
) {
    val displayText = buildString {
        append(text.take(shownChars))
        if (scrambling && shownChars < text.length) {
            val idx = ((scrambleSeed * 7 + shownChars * 3) % ScramblePool.size + ScramblePool.size) % ScramblePool.size
            append(ScramblePool[idx])
        }
    }
    Text(
        text = displayText,
        fontSize = 18.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = 16.sp,
        color = Color.White.copy(alpha = 0.88f * fade),
        fontFamily = FontFamily.Monospace,
    )
}
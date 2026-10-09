package com.gui.tomato.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** 上一次导航的方向。Tab 之间切换时由 AppNavHost 设置。 */
enum class NavDirection { Forward, Backward, None }

val LocalNavDirection = compositionLocalOf { NavDirection.None }

/**
 * 让内容以「错落」的方式入场。
 *
 * 入场方向由 [LocalNavDirection] 决定：
 * - Forward / Backward：横向归位（从切换方向那一侧）
 * - None：纵向轻微上移归位（默认，比如首次进入）
 */
@Composable
fun StaggerIn(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelayMs: Int = 90,
    durationMs: Int = 620,
    startOffsetY: Dp = 26.dp,
    startOffsetX: Dp = 56.dp,
    startScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val direction = LocalNavDirection.current
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index.coerceIn(0, 10) * baseDelayMs).toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
        )
    }

    val density = LocalDensity.current
    val offsetYPx = with(density) { startOffsetY.toPx() }
    val offsetXPx = with(density) { startOffsetX.toPx() }
    val p = progress.value

    val sign = when (direction) {
        NavDirection.Forward -> 1f
        NavDirection.Backward -> -1f
        NavDirection.None -> 0f
    }
    val useHorizontal = sign != 0f

    Box(
        modifier = modifier.graphicsLayer {
            alpha = p
            if (useHorizontal) {
                translationX = (1f - p) * offsetXPx * sign
            } else {
                translationY = (1f - p) * offsetYPx
            }
            if (startScale != 1f) {
                val s = startScale + (1f - startScale) * p
                scaleX = s
                scaleY = s
            }
        }
    ) {
        content()
    }
}
package com.gui.tomato.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 让内容以「错落」的方式入场：淡入 + 从下方轻微上移。
 *
 * @param index 出场顺序（0 最先，1 次之…）
 * @param baseDelayMs 每多一级延迟多少毫秒
 * @param durationMs 单个元素自己的动画时长
 * @param startOffsetY 起始下移距离
 * @param startScale 起始缩放（0.96 会有轻微“浮起”感）
 */
@Composable
fun StaggerIn(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelayMs: Int = 70,
    durationMs: Int = 460,
    startOffsetY: Dp = 26.dp,
    startScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index.coerceAtLeast(0) * baseDelayMs).toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
        )
    }
    val offsetPx = with(LocalDensity.current) { startOffsetY.toPx() }
    val p = progress.value

    Box(
        modifier = modifier.graphicsLayer {
            alpha = p
            translationY = (1f - p) * offsetPx
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
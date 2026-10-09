package com.gui.tomato.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * 通用橙渐变胶囊按钮。
 * - 按下：快速压缩到 scale 0.96，顶部叠暗（凹陷感）
 * - 释放：Spring Overshoot 轻微超调回弹
 */
@Composable
fun GuiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(50),
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
    gradient: Brush = Brush.linearGradient(
        colors = listOf(Color(0xFFE4572E), Color(0xFFFFA35C))
    ),
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // 压缩 / 回弹
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = if (pressed) {
            // 压下：快，几乎不弹
            spring(
                dampingRatio = 0.85f,
                stiffness = Spring.StiffnessHigh,
            )
        } else {
            // 释放：中等阻尼 → 轻微超调
            spring(
                dampingRatio = 0.45f,
                stiffness = Spring.StiffnessMediumLow,
            )
        },
        label = "GuiButtonScale",
    )

    // 内阴影强度
    val shadowAlpha by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = if (pressed) tween(90) else tween(220),
        label = "GuiButtonShadowAlpha",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(gradient, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .drawWithContent {
                drawContent()
                if (shadowAlpha > 0f) {
                    // 顶部压暗：凹陷感
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.30f * shadowAlpha),
                                Color.Transparent,
                            ),
                            startY = 0f,
                            endY = size.height,
                        ),
                        size = size,
                    )
                    // 底部极轻高光：受光被遮挡的微妙感
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.06f * shadowAlpha),
                            ),
                            startY = 0f,
                            endY = size.height,
                        ),
                        size = size,
                    )
                }
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content,
        )
    }
}
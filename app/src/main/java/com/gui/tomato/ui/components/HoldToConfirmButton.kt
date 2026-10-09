package com.gui.tomato.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gui.tomato.util.Haptic
import kotlinx.coroutines.launch

/**
 * 长按确认按钮：
 * - 按住时沿按钮边缘绘制进度环
 * - 未满松手 → 平滑回退
 * - 满 → 触发 onConfirm
 * - hapticsEnabled=true 时：按下瞬间短震，进度满触发双击
 */
@Composable
fun HoldToConfirmButton(
    label: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    holdDurationMs: Int = 800,
    hapticsEnabled: Boolean = true,
    bg: Color = Color(0xFF1A1A1A),
    contentColor: Color = Color(0xFFE4572E),
    ringColor: Color = Color(0xFFE4572E),
) {
    val ctx = LocalContext.current
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .drawWithContent {
                drawContent()
                val p = progress.value
                if (p > 0f) {
                    val cornerR = size.height / 2f
                    val outlinePath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(Offset.Zero, Size(size.width, size.height)),
                                cornerRadius = CornerRadius(cornerR, cornerR),
                            )
                        )
                    }
                    val pm = PathMeasure().apply { setPath(outlinePath, false) }
                    val len = pm.length
                    val seg = Path()
                    pm.getSegment(0f, len * p, seg, true)
                    drawPath(
                        path = seg,
                        color = ringColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // 按下瞬间：短震
                        if (hapticsEnabled) Haptic.tap(ctx)

                        val job = scope.launch {
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(holdDurationMs, easing = LinearEasing),
                            )
                            // 进度满：双击（"确认"感）
                            if (hapticsEnabled) Haptic.doubleTap(ctx)
                            onConfirm()
                        }
                        tryAwaitRelease()
                        job.cancel()
                        if (progress.value < 1f) {
                            progress.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(200, easing = FastOutSlowInEasing),
                            )
                        } else {
                            progress.snapTo(0f)
                        }
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp,
            )
        }
    }
}
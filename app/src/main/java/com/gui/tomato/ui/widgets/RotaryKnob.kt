package com.gui.tomato.ui.widgets

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gui.tomato.util.Haptic
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private val OrangeStart = Color(0xFFE4572E)
private val OrangeEnd = Color(0xFFFFA35C)

private fun createTickTrack(): AudioTrack? {
    return try {
        val sampleRate = 44100
        val durationMs = 22
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        val freq = 880.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = 1.0 - i.toDouble() / numSamples
            val s = sin(2.0 * Math.PI * freq * t) * Short.MAX_VALUE * 0.30 * env
            samples[i] = s.toInt().toShort()
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(numSamples * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, numSamples)
        track
    } catch (_: Exception) {
        null
    }
}

@Composable
fun RotaryKnob(
    value: Int,
    onChange: (Int) -> Unit,
    min: Int,
    max: Int,
    step: Int = 1,
    unit: String = "",
    label: String = "",
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    numberSizeSp: Int = 48,
    labelSizeSp: Int = 11
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val currentValue by rememberUpdatedState(value)

    val tickTrack = remember { createTickTrack() }
    DisposableEffect(Unit) {
        onDispose { tickTrack?.release() }
    }

    // 每个值一根刻度
    val tickCount = (max - min + 1).coerceAtLeast(2)

    BoxWithConstraints(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val sizeDp = maxWidth
        val sizePx = with(density) { sizeDp.toPx() }

        val bigKnob = sizePx >= with(density) { 180.dp.toPx() }

        val trackColor = if (isDark) Color(0xFF262626) else Color(0xFFEAEAEA)
        val textColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0D0D0D)
        val subColor = if (isDark) Color(0xFF8A8A8A) else Color(0xFF666666)

        val normalized = ((value - min).toFloat() / (max - min).toFloat()).coerceIn(0f, 1f)
        // 圆点吸附到最近刻度线上
        val dotPos = (normalized * (tickCount - 1)).roundToInt().toFloat() / (tickCount - 1)

        val dotRadius = if (bigKnob) 7.dp else 4.dp
        val dotInner = if (bigKnob) 3.dp else 1.5.dp
        val tickMajor = if (bigKnob) 2.5.dp else 1.6.dp
        val tickMinor = if (bigKnob) 1.2.dp else 0.9.dp

        // 数字块统一下移量：小旋钮也向大旋钮看齐，让数字离刻度更远
        val textOffsetY = if (bigKnob) 10.dp else 14.dp

        fun fireFeedback() {
            Haptic.tap(context)
            tickTrack?.let { t ->
                try {
                    t.stop()
                    t.reloadStaticData()
                    t.play()
                } catch (_: Exception) {
                }
            }
        }

        fun handleTouch(pos: Offset) {
            val cx = sizePx / 2f
            val cy = sizePx / 2f
            val dx = pos.x - cx
            val dy = pos.y - cy
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < sizePx * 0.10f) return

            var angle = Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble())).toFloat()
            angle = angle.coerceIn(-135f, 135f)

            val t = (angle + 135f) / 270f
            var newValue = min + (t * (max - min)).roundToInt()

            if (step > 1) {
                newValue = ((newValue.toFloat() / step).roundToInt() * step)
            }
            newValue = newValue.coerceIn(min, max)

            if (newValue != currentValue) {
                fireFeedback()
                onChange(newValue)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(min, max, step, sizePx) {
                    detectDragGestures(
                        onDragStart = { offset -> handleTouch(offset) },
                        onDrag = { change, _ ->
                            handleTouch(change.position)
                            change.consume()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val radius = this.size.minDimension / 2f - 6.dp.toPx()

                val tickMajorLen = if (bigKnob) 11.dp.toPx() else 6.dp.toPx()
                val tickMinorLen = if (bigKnob) 6.dp.toPx() else 3.5.dp.toPx()
                for (i in 0 until tickCount) {
                    val a = -135f + (i.toFloat() / (tickCount - 1)) * 270f
                    val rad = Math.toRadians((a - 90f).toDouble())
                    val isMajor = i % 5 == 0
                    val tickLen = if (isMajor) tickMajorLen else tickMinorLen
                    val sx = cx + (radius - tickLen) * cos(rad).toFloat()
                    val sy = cy + (radius - tickLen) * sin(rad).toFloat()
                    val ex = cx + radius * cos(rad).toFloat()
                    val ey = cy + radius * sin(rad).toFloat()
                    val tickPos = i.toFloat() / (tickCount - 1)
                    val done = tickPos <= normalized
                    val tickColor = if (done) OrangeStart else trackColor
                    drawLine(
                        color = tickColor,
                        start = Offset(sx, sy),
                        end = Offset(ex, ey),
                        strokeWidth = if (isMajor) tickMajor.toPx() else tickMinor.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                val arcR = radius - (if (bigKnob) 18.dp.toPx() else 11.dp.toPx())
                val arcStroke = if (bigKnob) 4.dp.toPx() else 2.5.dp.toPx()
                drawArc(
                    color = trackColor,
                    startAngle = -225f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(cx - arcR, cy - arcR),
                    size = androidx.compose.ui.geometry.Size(arcR * 2f, arcR * 2f),
                    style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                )
                if (normalized > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(OrangeStart, OrangeEnd)
                        ),
                        startAngle = -225f,
                        sweepAngle = 270f * normalized,
                        useCenter = false,
                        topLeft = Offset(cx - arcR, cy - arcR),
                        size = androidx.compose.ui.geometry.Size(arcR * 2f, arcR * 2f),
                        style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                    )
                }

                val a = -135f + 270f * dotPos
                val rad = Math.toRadians((a - 90f).toDouble())
                val dotX = cx + radius * cos(rad).toFloat()
                val dotY = cy + radius * sin(rad).toFloat()
                drawCircle(
                    color = OrangeStart,
                    radius = dotRadius.toPx(),
                    center = Offset(dotX, dotY)
                )
                drawCircle(
                    color = Color.White,
                    radius = dotInner.toPx(),
                    center = Offset(dotX, dotY)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.offset(y = textOffsetY)
            ) {
                Text(
                    text = value.toString(),
                    fontSize = numberSizeSp.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    color = textColor,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                if (unit.isNotEmpty()) {
                    Spacer(Modifier.height(if (bigKnob) 4.dp else 1.dp))
                    Text(
                        text = unit,
                        fontSize = (numberSizeSp * 0.30f).sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp,
                        color = subColor
                    )
                }
                if (label.isNotEmpty()) {
                    Spacer(Modifier.height(if (bigKnob) 8.dp else 3.dp))
                    Text(
                        text = label,
                        fontSize = labelSizeSp.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp,
                        color = subColor
                    )
                }
            }
        }
    }
}
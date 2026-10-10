package com.gui.tomato.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * 番茄钟到点提示音 —— AudioTrack 合成，不用系统铃声。
 *  0 → 暮鼓（沉钟，单声长音）
 *  1 → 松风（两声柔拨弦，第二声后快速淡出）
 *  2 → 涧泉（三声柔拨弦，间隔大）
 */
object SoundPlayer {

    private val main = Handler(Looper.getMainLooper())
    private const val SR = 44100

    private var t0: AudioTrack? = null
    private var t1: AudioTrack? = null
    private var t2: AudioTrack? = null
    private var initialized = false

    fun play(idx: Int) {
        ensureInit()
        val t = when (idx) {
            1 -> t1
            2 -> t2
            else -> t0
        } ?: return
        main.post {
            try {
                t.stop()
                t.reloadStaticData()
                t.play()
            } catch (_: Exception) {
            }
        }
    }

    private fun ensureInit() {
        if (initialized) return
        initialized = true
        t0 = build(buildDrum())
        t1 = build(buildPineWind())
        t2 = build(buildStream())
    }

    private fun build(samples: ShortArray): AudioTrack? {
        return try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SR)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(samples, 0, samples.size)
            track
        } catch (_: Exception) {
            null
        }
    }

    // ── 音效 0：暮鼓（440Hz 基频 + 泛音，1.5s 指数衰减）──
    private fun buildDrum(): ShortArray {
        val n = SR * 1500 / 1000
        val out = ShortArray(n)
        val freqs = doubleArrayOf(440.0, 880.0, 1320.0, 1760.0)
        val amps  = doubleArrayOf(1.0, 0.55, 0.28, 0.15)
        for (i in 0 until n) {
            val t = i.toDouble() / SR
            val env = exp(-3.2 * t)
            var s = 0.0
            for (k in freqs.indices) {
                s += amps[k] * sin(2.0 * PI * freqs[k] * t)
            }
            out[i] = (s * Short.MAX_VALUE * 0.32 * env)
                .toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    // ── 音效 1：松风（第一声柔拨长尾，第二声 deng 后留短尾）──
    private fun buildPineWind(): ShortArray {
        return mixSamples(
            totalMs = 1800,
            parts = listOf(
                Pluck(startMs = 0,   freq = 196.00, durMs = 1200, damping = 0.9995, amp = 0.75),
                Pluck(startMs = 700, freq = 261.63, durMs = 780,  damping = 0.9960, amp = 0.72)
            )
        )
    }

    // ── 音效 2：涧泉（三声柔拨弦，间隔 450ms）──
    private fun buildStream(): ShortArray {
        return mixSamples(
            totalMs = 2200,
            parts = listOf(
                Pluck(startMs = 0,   freq = 261.63, durMs = 1400, damping = 0.9990, amp = 0.70),
                Pluck(startMs = 450, freq = 329.63, durMs = 1300, damping = 0.9990, amp = 0.68),
                Pluck(startMs = 900, freq = 392.00, durMs = 1200, damping = 0.9990, amp = 0.66)
            )
        )
    }

    private data class Pluck(
        val startMs: Int,
        val freq: Double,
        val durMs: Int,
        val damping: Double,
        val amp: Double,
        val pluckPos: Double = 0.28
    )

    private fun mixSamples(totalMs: Int, parts: List<Pluck>): ShortArray {
        val n = SR * totalMs / 1000
        val out = ShortArray(n)
        for (p in parts) {
            val samples = karplus(p)
            val start = SR * p.startMs / 1000
            for (i in samples.indices) {
                val idx = start + i
                if (idx >= n) break
                val mixed = out[idx] + samples[i]
                out[idx] = mixed.coerceIn(-32768, 32767).toShort()
            }
        }
        return out
    }

    // Karplus-Strong 拨弦合成（柔化版）
    private fun karplus(p: Pluck): ShortArray {
        val n = SR * p.durMs / 1000
        val out = ShortArray(n)
        val period = (SR / p.freq).toInt().coerceAtLeast(2)
        val buf = DoubleArray(period)

        val rnd = Random(42L + (p.freq * 10).toLong())
        // 初始缓冲：正弦为主 + 少量噪声 → 起音更柔、不"啪"
        for (i in 0 until period) {
            val t = i.toDouble() / period
            val sine = sin(2.0 * PI * t)
            val noise = rnd.nextDouble() * 2.0 - 1.0
            buf[i] = sine * 0.85 + noise * 0.15
        }
        // 梳状滤波模拟拨弦位置
        val delay = (period * p.pluckPos).toInt().coerceIn(1, period - 1)
        for (i in 0 until period) {
            val j = (i + delay) % period
            buf[i] = (buf[i] + buf[j]) * 0.5
        }

        var idx = 0
        for (i in 0 until n) {
            val cur = buf[idx]
            val nxt = buf[(idx + 1) % period]
            out[i] = (cur * Short.MAX_VALUE * p.amp)
                .toInt().coerceIn(-32768, 32767).toShort()
            buf[idx] = (cur + nxt) * 0.5 * p.damping
            idx = (idx + 1) % period
        }

        // 起音 35ms 淡入
        val attackLen = (SR * 0.035).toInt()
        for (i in 0 until attackLen) {
            if (i in 0 until n) {
                val a = i.toDouble() / attackLen
                out[i] = (out[i] * a).toInt().toShort()
            }
        }

        // 尾部 40ms 淡出，防硬切
        val fadeLen = (SR * 0.040).toInt()
        for (i in 0 until fadeLen) {
            val pos = n - fadeLen + i
            if (pos in 0 until n) {
                val f = 1.0 - i.toDouble() / fadeLen
                out[pos] = (out[pos] * f).toInt().toShort()
            }
        }
        return out
    }
}
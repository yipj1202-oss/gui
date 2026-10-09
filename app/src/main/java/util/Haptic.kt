package com.gui.tomato.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * 振动工具。所有振动统一走这里，方便以后调时长/强度。
 * 调用前请自行判断 prefs.vibrate 开关。
 */
object Haptic {

    /** 短震一次，默认 25ms */
    fun tap(context: Context, durationMs: Long = 25) {
        val vib = getVibrator(context) ?: return
        if (!vib.hasVibrator()) return
        vib.vibrate(
            VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    /** 长震，用于「阶段结束」之类的强提醒，默认 400ms */
    fun buzz(context: Context, durationMs: Long = 400) {
        tap(context, durationMs)
    }

    /** 双击振动：短-停-短，用于「该休息了 / 该专注了」 */
    fun doubleTap(context: Context) {
        val vib = getVibrator(context) ?: return
        if (!vib.hasVibrator()) return
        vib.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 80, 120, 80),
                -1
            )
        )
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
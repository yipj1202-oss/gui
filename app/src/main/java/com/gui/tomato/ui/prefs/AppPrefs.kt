package com.gui.tomato.ui.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AppPrefs(ctx: Context) {
    private val sp: SharedPreferences = ctx.getSharedPreferences("gui_prefs", Context.MODE_PRIVATE)

    private var themeState by mutableIntStateOf(sp.getInt("theme", 0))
    var theme: Int
        get() = themeState
        set(v) {
            themeState = v
            sp.edit().putInt("theme", v).apply()
        }

    private var langState by mutableIntStateOf(sp.getInt("lang", 0))
    var lang: Int
        get() = langState
        set(v) {
            langState = v
            sp.edit().putInt("lang", v).apply()
        }

    var defaultPresetId: Long
        get() = sp.getLong("defPreset", -1L)
        set(v) = sp.edit().putLong("defPreset", v).apply()
    var notifyEnabled: Boolean
        get() = sp.getBoolean("notify", true)
        set(v) = sp.edit().putBoolean("notify", v).apply()
    var vibrate: Boolean
        get() = sp.getBoolean("vibrate", true)
        set(v) = sp.edit().putBoolean("vibrate", v).apply()

    // ── 音效 ──
    private var soundEnabledState by mutableStateOf(sp.getBoolean("soundOn", true))
    var soundEnabled: Boolean
        get() = soundEnabledState
        set(v) {
            soundEnabledState = v
            sp.edit().putBoolean("soundOn", v).apply()
        }

    private var soundIdxState by mutableIntStateOf(sp.getInt("soundIdx", 0))
    var soundIdx: Int
        get() = soundIdxState
        set(v) {
            soundIdxState = v
            sp.edit().putInt("soundIdx", v).apply()
        }

    // ── OVERCLOCK 狂暴方案 id（-1 表示未设置）──
    private var overclockPresetIdState by mutableLongStateOf(sp.getLong("overclockPresetId", -1L))
    var overclockPresetId: Long
        get() = overclockPresetIdState
        set(v) {
            overclockPresetIdState = v
            sp.edit().putLong("overclockPresetId", v).apply()
        }

    // ── 上次运行时的 versionCode（-1 表示首次安装）──
    var lastRunVersionCode: Int
        get() = sp.getInt("lastRunVc", -1)
        set(v) = sp.edit().putInt("lastRunVc", v).apply()
}
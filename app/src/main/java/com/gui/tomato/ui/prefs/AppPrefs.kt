package com.gui.tomato.ui.prefs

import android.content.Context
import android.content.SharedPreferences

class AppPrefs(ctx: Context) {
    private val sp: SharedPreferences = ctx.getSharedPreferences("gui_prefs", Context.MODE_PRIVATE)

    var theme: Int
        get() = sp.getInt("theme", 0) // 0=light, 1=dark
        set(v) = sp.edit().putInt("theme", v).apply()
    var lang: Int
        get() = sp.getInt("lang", 0) // 0=zh, 1=en
        set(v) = sp.edit().putInt("lang", v).apply()
    var defaultPresetId: Long
        get() = sp.getLong("defPreset", -1L)
        set(v) = sp.edit().putLong("defPreset", v).apply()
    var notifyEnabled: Boolean
        get() = sp.getBoolean("notify", true)
        set(v) = sp.edit().putBoolean("notify", v).apply()
    var vibrate: Boolean
        get() = sp.getBoolean("vibrate", true)
        set(v) = sp.edit().putBoolean("vibrate", v).apply()
}

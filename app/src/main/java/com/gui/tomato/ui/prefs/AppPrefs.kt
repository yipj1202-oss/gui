package com.gui.tomato.ui.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
}
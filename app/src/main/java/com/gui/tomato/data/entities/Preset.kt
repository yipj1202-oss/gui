package com.gui.tomato.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PomodoroPreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val focusMin: Int = 25,
    val shortBreakMin: Int = 5,
    val longBreakEvery: Int = 4,
    val longBreakMin: Int = 15,
    val isDefault: Boolean = false
)

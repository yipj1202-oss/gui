package com.gui.tomato.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetId: Long,
    val taskId: Long?,
    val startedAt: Long,
    val endedAt: Long?,
    val type: String,          // "focus" | "shortBreak" | "longBreak"
    val plannedMin: Int,
    val completedMin: Int,
    val finishedByTime: Boolean
)

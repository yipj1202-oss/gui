package com.gui.tomato.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val createdAt: Long,
    val completedAt: Long? = null,
    val completed: Boolean = false
)

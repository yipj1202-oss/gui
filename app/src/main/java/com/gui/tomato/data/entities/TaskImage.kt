package com.gui.tomato.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_images")
data class TaskImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val filePath: String,
    val idx: Int = 0
)

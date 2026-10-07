package com.gui.tomato.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gui.tomato.data.dao.*
import com.gui.tomato.data.entities.*

@Database(
    entities = [Task::class, TaskImage::class, PomodoroPreset::class, PomodoroSession::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun presetDao(): PresetDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(ctx: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(ctx, AppDatabase::class.java, "gui.db")
                    .fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

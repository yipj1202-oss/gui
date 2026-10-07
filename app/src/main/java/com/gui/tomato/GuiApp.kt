package com.gui.tomato

import android.app.Application
import com.gui.tomato.data.db.AppDatabase
import com.gui.tomato.service.PomodoroRepository

class GuiApp : Application() {
    lateinit var db: AppDatabase
    lateinit var repo: PomodoroRepository
        private set

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.get(this)
        repo = PomodoroRepository(db)
    }
}

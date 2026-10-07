package com.gui.tomato

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.gui.tomato.data.StatsRepo
import com.gui.tomato.data.TaskStatRow
import com.gui.tomato.data.backup.BackupStore
import com.gui.tomato.data.db.AppDatabase
import com.gui.tomato.service.PomodoroRepository
import com.gui.tomato.service.TimerForegroundService
import kotlinx.coroutines.runBlocking

class GuiApp : Application() {
    lateinit var db: AppDatabase
    lateinit var repo: PomodoroRepository
        private set
    private var backupStore: BackupStore? = null

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.get(this)
        repo = PomodoroRepository(db)
        repo.recoverOnStartup()
    }

    fun serviceStart() { if (repo.ui.value.state != com.gui.tomato.engine.State.IDLE) TimerForegroundService.start(this) }
    fun serviceStop() { TimerForegroundService.stop(this) }

    fun todayFocusCount(): Int = runBlocking {
        val dayStart = dayStartMs()
        db.sessionDao().focusInRange(dayStart, Long.MAX_VALUE).count { it.type == "focus" }
    }
    fun todayFocusMinutes(): Int = runBlocking {
        val dayStart = dayStartMs()
        db.sessionDao().focusInRange(dayStart, Long.MAX_VALUE).sumOf { it.completedMin }
    }
    private fun dayStartMs(): Long {
        val c = java.util.Calendar.getInstance()
        c.set(java.util.Calendar.HOUR_OF_DAY, 0)
        c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0)
        c.set(java.util.Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun todayFocusCountSafe(): Int =
        runBlocking { db.sessionDao().focusInRange(dayStartMs(), Long.MAX_VALUE).count { it.type == "focus" } }
    fun todayFocusMinutesSafe(): Int =
        runBlocking { db.sessionDao().focusInRange(dayStartMs(), Long.MAX_VALUE).sumOf { it.completedMin } }

    // Stats safe wrappers used by UI (coroutine-free)
    fun byTaskSafe(from: Long, to: Long): List<TaskStatRow> =
        runBlocking { StatsRepo(db.sessionDao(), db.taskDao()).byTask(from, to) }
    fun byHourSafe(from: Long, to: Long): List<com.gui.tomato.data.HourStatRow> =
        runBlocking { StatsRepo(db.sessionDao(), db.taskDao()).byHour(from, to) }

    // Backup helpers
    fun backupToPublic() {
        val bs = backupStore ?: BackupStore(this, db).also { backupStore = it }
        val dir = android.os.Environment.getExternalStorageDirectory()
        val out = java.io.File(dir, "gui_backup.guiback")
        try {
            bs.export(out)
            Toast.makeText(this, "已导出到 ${out.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun backupImport(uri: Uri): String {
        val tmp = java.io.File(cacheDir, "import.guiback")
        contentResolver.openInputStream(uri)?.use { it.copyTo(tmp.outputStream()) } ?: return "no uri"
        val bs = backupStore ?: BackupStore(this, db).also { backupStore = it }
        return try { bs.import(tmp, overwrite = true) } catch (e: Exception) { "import failed: ${e.message}" }
    }
}

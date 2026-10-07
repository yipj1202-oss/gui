package com.gui.tomato.service

import com.gui.tomato.data.db.AppDatabase
import com.gui.tomato.data.entities.PomodoroPreset
import com.gui.tomato.data.entities.PomodoroSession
import com.gui.tomato.engine.PomodoroEngine
import com.gui.tomato.engine.Snapshot
import com.gui.tomato.engine.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

class PomodoroRepository(private val db: AppDatabase) {
    private val io = CoroutineScope(Dispatchers.IO)
    private val mutable = MutableStateFlow(Snapshot(State.IDLE, 0L, null, 1L, 0, 0L, 0L, 0, "idle"))
    val ui: StateFlow<Snapshot> = mutable

    val engine = PomodoroEngine(
        onSessionInsert = { presetId, taskId, startedAt, type, plannedMin ->
            io.launch {
                db.sessionDao().insert(
                    PomodoroSession(presetId = presetId, taskId = taskId, startedAt = startedAt,
                        endedAt = null, type = type, plannedMin = plannedMin, completedMin = 0, finishedByTime = false)
                )
            }
        },
        onSessionClose = { startedAt, endedAt, completedMin, ftb ->
            io.launch {
                val open = db.sessionDao().getOpen()
                if (open != null) db.sessionDao().close(open.id, endedAt, completedMin, ftb)
            }
        }
    )

    fun current(): Snapshot = engine.currentSnapshot(System.currentTimeMillis()).also {
        if (it != mutable.value) mutable.value = it
    }

    fun start(taskId: Long?) {
        val p = runBlocking { db.presetDao().getDefault() ?: defaultPreset() }
        engine.applyPreset(p)
        engine.start(taskId, System.currentTimeMillis())
        current()
    }
    fun skip() { engine.skipSegment(System.currentTimeMillis()); current() }
    fun finish() {
        engine.finishAll(System.currentTimeMillis())
        // close the currently-open session so it isn't left dangling
        io.launch {
            val open = db.sessionDao().getOpen()
            if (open != null) db.sessionDao().close(open.id, System.currentTimeMillis(), open.completedMin, true)
        }
        current()
    }

    /**
     * On cold start, if a focus session was left open (app killed mid-timer),
     * close it as finished-by-time and re-apply the last preset.
     */
    fun recoverOnStartup() {
        runBlocking {
            val open = db.sessionDao().getOpen()
            if (open != null) {
                db.sessionDao().close(open.id, System.currentTimeMillis(), open.completedMin, true)
            }
            db.presetDao().getDefault()?.let { engine.applyPreset(it) }
        }
        current()
    }

    private fun defaultPreset() =
        PomodoroPreset(name = "默认", focusMin = 25, shortBreakMin = 5, longBreakEvery = 4, longBreakMin = 15)
}
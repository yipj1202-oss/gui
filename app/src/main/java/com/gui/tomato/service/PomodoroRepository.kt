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
        engine.applyPreset(runBlocking { db.presetDao().getDefault() ?: defaultPreset() })
        engine.start(taskId, System.currentTimeMillis())
        current()
    }
    fun skip() { engine.skipSegment(System.currentTimeMillis()); current() }
    fun finish() { engine.finishAll(System.currentTimeMillis()); current() }

    fun recoverOnStartup() {
        // If a session was open (app killed mid-focus), let the engine recompute.
        runBlocking {
            val open = db.sessionDao().getOpen()
            if (open != null) {
                // Mark it finished-by-time if it had already elapsed; else leave open.
                db.sessionDao().close(open.id, System.currentTimeMillis(), open.completedMin, true)
                io.launch { db.sessionDao().clearOpen() }
            }
        }
        current()
    }

    private fun defaultPreset() =
        PomodoroPreset(name = "默认", focusMin = 25, shortBreakMin = 5, longBreakEvery = 4, longBreakMin = 15)
}

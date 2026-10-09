package com.gui.tomato.service

import android.content.Context
import com.gui.tomato.data.db.AppDatabase
import com.gui.tomato.data.entities.PomodoroPreset
import com.gui.tomato.data.entities.PomodoroSession
import com.gui.tomato.engine.EngineEvent
import com.gui.tomato.engine.PomodoroEngine
import com.gui.tomato.engine.Snapshot
import com.gui.tomato.engine.State
import com.gui.tomato.ui.prefs.AppPrefs
import com.gui.tomato.util.Haptic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

class PomodoroRepository(
    private val appContext: Context,
    private val db: AppDatabase,
) {
    private val io = CoroutineScope(Dispatchers.IO)
    private val prefs = AppPrefs(appContext)
    private val mutable = MutableStateFlow(Snapshot(State.IDLE, 0L, null, 1L, 0, 0L, 0L, 0, "idle", false))
    val ui: StateFlow<Snapshot> = mutable

    val engine = PomodoroEngine(
        onSessionInsert = { presetId, taskId, startedAt, type, plannedMin ->
            io.launch {
                db.sessionDao().insert(
                    PomodoroSession(
                        presetId = presetId,
                        taskId = taskId,
                        startedAt = startedAt,
                        endedAt = null,
                        type = type,
                        plannedMin = plannedMin,
                        completedMin = 0,
                        finishedByTime = false
                    )
                )
            }
        },
        onSessionClose = { startedAt, endedAt, completedMin, ftb ->
            io.launch {
                val open = db.sessionDao().getOpen()
                if (open != null) db.sessionDao().close(open.id, endedAt, completedMin, ftb)
            }
        },
        onEvent = { event ->
            if (prefs.vibrate) {
                when (event) {
                    EngineEvent.Started        -> Haptic.tap(appContext)
                    EngineEvent.Paused         -> Haptic.tap(appContext)
                    EngineEvent.Resumed        -> Haptic.tap(appContext)
                    EngineEvent.Skipped        -> Haptic.tap(appContext)
                    EngineEvent.Finished       -> Haptic.buzz(appContext, 300)
                    EngineEvent.PhaseCompleted -> Haptic.doubleTap(appContext)
                }
            }
        }
    )

    fun current(): Snapshot = engine.currentSnapshot(System.currentTimeMillis()).also {
        mutable.value = it
    }

    fun applyPreset(p: PomodoroPreset) {
        engine.applyPreset(p)
        mutable.value = engine.currentSnapshot(System.currentTimeMillis())
    }

    fun start(taskId: Long?) {
        engine.start(taskId, System.currentTimeMillis())
        current()
    }

    fun pause() {
        engine.pause(System.currentTimeMillis())
        current()
    }

    fun resume() {
        engine.resume(System.currentTimeMillis())
        current()
    }

    fun skip() {
        engine.skipSegment(System.currentTimeMillis())
        current()
    }

    fun finish() {
        engine.finishAll(System.currentTimeMillis())
        io.launch {
            val open = db.sessionDao().getOpen()
            if (open != null) db.sessionDao().close(open.id, System.currentTimeMillis(), open.completedMin, true)
        }
        current()
    }

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
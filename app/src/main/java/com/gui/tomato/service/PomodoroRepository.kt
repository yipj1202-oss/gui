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
import com.gui.tomato.util.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PomodoroRepository(
    private val appContext: Context,
    private val db: AppDatabase,
) {
    private val io = CoroutineScope(Dispatchers.IO)
    private val prefs = AppPrefs(appContext)
    private val mutable = MutableStateFlow(Snapshot(State.IDLE, 0L, null, 1L, 0, 0L, 0L, 0, "idle", false))
    val ui: StateFlow<Snapshot> = mutable

    // 狂暴模式状态
    private val _isOverclock = MutableStateFlow(false)
    val isOverclock: StateFlow<Boolean> = _isOverclock

    // 缓存"默认方案"，用于退出狂暴时同步切回
    private var cachedDefaultPreset: PomodoroPreset? = null

    // 反向引用，避免在 engine 自己的初始化里引用自身
    private var engineRef: PomodoroEngine? = null

    val engine: PomodoroEngine = PomodoroEngine(
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
            // 阶段结束（专注/休息到点）→ 播放提示音
            if (event == EngineEvent.PhaseCompleted && prefs.soundEnabled) {
                SoundPlayer.play(prefs.soundIdx)
            }

            // 自动退出狂暴：一个完整周期 = FOCUS + 后续 BREAK。
            // PhaseCompleted 触发时 state 仍是"刚刚结束的那一段"，所以看到
            // SHORT_BREAK/LONG_BREAK 就说明 BREAK 刚跑完、即将回到 FOCUS，即一整个周期结束。
            if (event == EngineEvent.PhaseCompleted && _isOverclock.value) {
                val eng = engineRef
                if (eng != null) {
                    val s = eng.currentState()
                    if (s == State.SHORT_BREAK || s == State.LONG_BREAK) {
                        _isOverclock.value = false
                        cachedDefaultPreset?.let { eng.applyPreset(it) }
                    }
                }
            }
        }
    ).also { engineRef = it }

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

    /** 启动狂暴模式：切到狂暴方案 + 标记状态 + 开跑（UI 层负责传 preset） */
    fun activateOverclock(preset: PomodoroPreset, taskId: Long?) {
        engine.applyPreset(preset)
        _isOverclock.value = true
        engine.start(taskId, System.currentTimeMillis())
        current()
    }

    /** 手动退出狂暴：切回默认方案（不打断当前计时） */
    fun exitOverclock() {
        _isOverclock.value = false
        cachedDefaultPreset?.let { engine.applyPreset(it) }
        current()
    }

    fun recoverOnStartup() {
        runBlocking {
            val open = db.sessionDao().getOpen()
            if (open != null) {
                db.sessionDao().close(open.id, System.currentTimeMillis(), open.completedMin, true)
            }
            val def = db.presetDao().getDefault()
            cachedDefaultPreset = def
            def?.let { engine.applyPreset(it) }
        }
        current()
    }

    private fun defaultPreset() =
        PomodoroPreset(name = "默认", focusMin = 25, shortBreakMin = 5, longBreakEvery = 4, longBreakMin = 15)
}
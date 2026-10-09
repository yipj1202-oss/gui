package com.gui.tomato.engine

import com.gui.tomato.data.entities.PomodoroPreset

enum class State { IDLE, FOCUS, SHORT_BREAK, LONG_BREAK }

data class Snapshot(
    val state: State,
    val remainMs: Long,
    val taskId: Long?,
    val presetId: Long,
    val segmentIndex: Int,
    val startedAt: Long,
    val plannedMs: Long,
    val cyclePos: Int,
    val type: String,
    val isPaused: Boolean = false
)

class PomodoroEngine(
    private val onSessionInsert: (presetId: Long, taskId: Long?, startedAt: Long, type: String, plannedMin: Int) -> Unit = { _, _, _, _, _ -> },
    private val onSessionClose: (startedAt: Long, endedAt: Long, completedMin: Int, finishedByTime: Boolean) -> Unit = { _, _, _, _ -> }
) {
    var preset: PomodoroPreset = PomodoroPreset(name = "default", focusMin = 25, shortBreakMin = 5, longBreakEvery = 4, longBreakMin = 15)
        private set
    private var activeTaskId: Long? = null
    private var state: State = State.IDLE
    private var segStartMs: Long = 0L
    private var segPlannedMs: Long = 0L
    private var segType: String = "focus"
    private var cyclePos: Int = 0
    private var totalFocusCount: Int = 0
    private var running: Boolean = false

    private var isPaused: Boolean = false
    private var pausedAtMs: Long = 0L
    private var totalPausedMs: Long = 0L

    fun applyPreset(p: PomodoroPreset) { preset = p }
    fun taskId(): Long? = activeTaskId
    fun isRunning(): Boolean = running
    fun isPaused(): Boolean = isPaused
    fun currentState(): State = if (running) state else State.IDLE

    fun start(taskId: Long?, nowMs: Long) {
        activeTaskId = taskId
        cyclePos = 0
        totalFocusCount = 0
        running = true
        startSegment(State.FOCUS, "focus", preset.focusMin, nowMs)
    }

    fun pause(nowMs: Long) {
        if (!running || isPaused) return
        isPaused = true
        pausedAtMs = nowMs
    }

    fun resume(nowMs: Long) {
        if (!running || !isPaused) return
        totalPausedMs += (nowMs - pausedAtMs)
        isPaused = false
        pausedAtMs = 0L
    }

    fun skipSegment(nowMs: Long) {
        if (!running) return
        closeCurrent(nowMs, finishedByTime = false)
        next(nowMs)
    }

    fun finishAll(nowMs: Long) {
        if (!running) return
        closeCurrent(nowMs, finishedByTime = true)
        running = false
        state = State.IDLE
        segStartMs = 0L
        isPaused = false
        pausedAtMs = 0L
        totalPausedMs = 0L
    }

    private fun startSegment(st: State, type: String, plannedMin: Int, nowMs: Long) {
        state = st
        segType = type
        segStartMs = nowMs
        segPlannedMs = plannedMin * 60_000L
        isPaused = false
        pausedAtMs = 0L
        totalPausedMs = 0L
        onSessionInsert(preset.id, activeTaskId, nowMs, type, plannedMin)
    }

    private fun closeCurrent(nowMs: Long, finishedByTime: Boolean) {
        val effectivePause = if (isPaused) (nowMs - pausedAtMs) else 0L
        val realElapsed = (nowMs - segStartMs) - totalPausedMs - effectivePause
        val completedMin = if (realElapsed > 0) (realElapsed / 60_000).toInt() else 0
        onSessionClose(segStartMs, nowMs, completedMin, finishedByTime)
    }

    private fun next(nowMs: Long) {
        when (state) {
            State.FOCUS -> {
                totalFocusCount++
                cyclePos++
                if (cyclePos % preset.longBreakEvery == 0) {
                    startSegment(State.LONG_BREAK, "longBreak", preset.longBreakMin, nowMs)
                } else {
                    startSegment(State.SHORT_BREAK, "shortBreak", preset.shortBreakMin, nowMs)
                }
            }
            else -> startSegment(State.FOCUS, "focus", preset.focusMin, nowMs)
        }
    }

    fun currentSnapshot(nowMs: Long): Snapshot {
        if (!running) {
            return Snapshot(State.IDLE, 0L, activeTaskId, preset.id, totalFocusCount, 0L, 0L, 0, "idle", false)
        }
        val effectivePause = if (isPaused) (nowMs - pausedAtMs) else 0L
        val totalPause = totalPausedMs + effectivePause
        val elapsed = (nowMs - segStartMs) - totalPause
        val remain = (segPlannedMs - elapsed).coerceAtLeast(0L)

        if (!isPaused && remain == 0L && elapsed >= segPlannedMs) {
            closeCurrent(nowMs, finishedByTime = true)
            val advanceAt = segStartMs + segPlannedMs + totalPausedMs
            next(advanceAt)
            val elapsed2 = (nowMs - segStartMs)
            val remain2 = (segPlannedMs - elapsed2).coerceAtLeast(0L)
            return Snapshot(state, remain2, activeTaskId, preset.id, totalFocusCount, segStartMs, segPlannedMs, cyclePos, segType, isPaused)
        }
        return Snapshot(state, remain, activeTaskId, preset.id, totalFocusCount, segStartMs, segPlannedMs, cyclePos, segType, isPaused)
    }
}

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
    val type: String
)

/**
 * Pure pomodoro state machine driven by timestamps (no tick loop).
 * currentSnapshot(now) is a pure function of (now) so it can recover after
 * the process is killed: segStartMs + plan recompute the remainder.
 */
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

    fun applyPreset(p: PomodoroPreset) { preset = p }
    fun taskId(): Long? = activeTaskId
    fun isRunning(): Boolean = running
    fun currentState(): State = if (running) state else State.IDLE

    fun start(taskId: Long?, nowMs: Long) {
        activeTaskId = taskId
        cyclePos = 0
        totalFocusCount = 0
        running = true
        startSegment(State.FOCUS, "focus", preset.focusMin, nowMs)
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
    }

    private fun startSegment(st: State, type: String, plannedMin: Int, nowMs: Long) {
        state = st
        segType = type
        segStartMs = nowMs
        segPlannedMs = plannedMin * 60_000L
        onSessionInsert(preset.id, activeTaskId, nowMs, type, plannedMin)
    }

    private fun closeCurrent(nowMs: Long, finishedByTime: Boolean) {
        val completedMin = if (nowMs > segStartMs) ((nowMs - segStartMs) / 60_000).toInt() else 0
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

    /** Pure snapshot for UI / service / widget / recovery. Auto-advances on elapsed. */
    fun currentSnapshot(nowMs: Long): Snapshot {
        if (!running) {
            return Snapshot(State.IDLE, 0L, activeTaskId, preset.id, totalFocusCount, 0L, 0L, 0, "idle")
        }
        val elapsed = nowMs - segStartMs
        val remain = (segPlannedMs - elapsed).coerceAtLeast(0L)
        if (remain == 0L && elapsed >= segPlannedMs) {
            closeCurrent(nowMs, finishedByTime = true)
            // Auto-advance at the natural boundary so the new segment's elapsed time is honored.
            val advanceAt = segStartMs + segPlannedMs
            next(advanceAt)
            val elapsed2 = nowMs - segStartMs
            val remain2 = (segPlannedMs - elapsed2).coerceAtLeast(0L)
            return Snapshot(state, remain2, activeTaskId, preset.id, totalFocusCount, segStartMs, segPlannedMs, cyclePos, segType)
        }
        return Snapshot(state, remain, activeTaskId, preset.id, totalFocusCount, segStartMs, segPlannedMs, cyclePos, segType)
    }
}

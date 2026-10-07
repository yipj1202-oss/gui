package com.gui.tomato

import com.gui.tomato.data.entities.PomodoroPreset
import com.gui.tomato.engine.PomodoroEngine
import com.gui.tomato.engine.State
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PomodoroEngineTest {
    private val eng = PomodoroEngine()
    private val preset = PomodoroPreset(
        id = 1, name = "test", focusMin = 1, shortBreakMin = 1,
        longBreakEvery = 2, longBreakMin = 2
    )

    @Before fun setUp() { eng.applyPreset(preset) }

    @Test fun focusCountsDownByTimestamp() {
        eng.start(taskId = null, nowMs = 0L)
        val snap = eng.currentSnapshot(nowMs = 30_000L)
        assertEquals(State.FOCUS, snap.state)
        assertEquals(30_000L, snap.remainMs)
    }

    @Test fun afterFocusGoesToShortBreak() {
        eng.start(taskId = null, nowMs = 0L)
        eng.skipSegment(nowMs = 60_000L)
        val snap = eng.currentSnapshot(nowMs = 61_000L)
        assertEquals(State.SHORT_BREAK, snap.state)
        assertEquals(59_000L, snap.remainMs)
    }

    @Test fun longBreakAfterEveryN() {
        eng.start(taskId = null, nowMs = 0L)
        eng.skipSegment(nowMs = 60_000L)  // focus1 -> short break
        eng.skipSegment(nowMs = 120_000L)  // break -> focus2
        eng.skipSegment(nowMs = 180_000L)  // focus2 -> long break (every 2)
        val snap = eng.currentSnapshot(nowMs = 181_000L)
        assertEquals(State.LONG_BREAK, snap.state)
        assertEquals(119_000L, snap.remainMs)
    }

    @Test fun killRecoveryRecomputesRemain() {
        eng.start(taskId = 5L, nowMs = 1_000_000L)
        val snap = eng.currentSnapshot(nowMs = 1_000_000L + 45_000L)
        assertEquals(State.FOCUS, snap.state)
        assertEquals(15_000L, snap.remainMs)
        assertEquals(5L, snap.taskId)
    }

    @Test fun autoAdvanceOnElapsed() {
        eng.start(taskId = null, nowMs = 0L)
        // 1min focus elapses without any call; currentSnapshot should auto-advance to break
        val snap = eng.currentSnapshot(nowMs = 80_000L)
        assertEquals(State.SHORT_BREAK, snap.state)
        assertEquals(40_000L, snap.remainMs) // 1min break started at t=60s, 20s elapsed -> 40s remain
    }

    @Test fun finishAllReturnsToIdle() {
        eng.start(taskId = null, nowMs = 0L)
        eng.finishAll(nowMs = 60_000L)
        val snap = eng.currentSnapshot(nowMs = 120_000L)
        assertEquals(State.IDLE, snap.state)
        assertFalse(eng.isRunning())
    }
}

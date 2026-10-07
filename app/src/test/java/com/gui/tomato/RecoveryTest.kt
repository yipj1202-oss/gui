package com.gui.tomato

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.gui.tomato.data.entities.PomodoroPreset
import com.gui.tomato.data.entities.PomodoroSession
import com.gui.tomato.data.db.AppDatabase
import com.gui.tomato.engine.PomodoroEngine
import com.gui.tomato.engine.State

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class RecoveryTest {
    private lateinit var db: AppDatabase

    @Before fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).allowMainThreadQueries().build()
    }
    @After fun tearDown() = db.close()

    @Test fun openSessionIsRecomputedOnColdStart() = runTest {
        // Seed a default preset and an open focus session that started 30s into a 1min focus
        val preset = db.presetDao().insert(PomodoroPreset(name = "d", focusMin = 1, shortBreakMin = 1, longBreakEvery = 4, longBreakMin = 2))
        val start = System.currentTimeMillis() - 30_000L
        db.sessionDao().insert(PomodoroSession(
            presetId = preset, taskId = null, startedAt = start, endedAt = null,
            type = "focus", plannedMin = 1, completedMin = 0, finishedByTime = false
        ))

        // Simulate the recovery path: close the open session as finished-by-time
        val open = db.sessionDao().getOpen()
        assertNotNull("an open session should be findable", open)
        db.sessionDao().close(open!!.id, System.currentTimeMillis(), open.completedMin, true)

        // After close there should be no open session
        assertNull(db.sessionDao().getOpen())
        // And the focus record should now be closed with finishedByTime
        val all = db.sessionDao().all()
        val closed = all.filter { it.endedAt != null }
        assertEquals(1, closed.size)
        assertTrue(closed[0].finishedByTime)
    }

    @Test fun engineRecomputesRemainAfterKill() {
        val preset = PomodoroPreset(name = "d", focusMin = 1, shortBreakMin = 1, longBreakEvery = 4, longBreakMin = 2)
        val eng = PomodoroEngine()
        eng.applyPreset(preset)
        eng.start(taskId = 7L, nowMs = 500_000L)
        // cold-start 20s later
        val snap = eng.currentSnapshot(nowMs = 520_000L)
        assertEquals(State.FOCUS, snap.state)
        assertEquals(40_000L, snap.remainMs)
        assertEquals(7L, snap.taskId)
    }
}
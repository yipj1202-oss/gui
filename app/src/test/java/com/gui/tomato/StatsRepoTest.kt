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
import com.gui.tomato.data.StatsRepo
import com.gui.tomato.data.entities.PomodoroSession
import com.gui.tomato.data.entities.Task
import com.gui.tomato.data.db.AppDatabase

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class StatsRepoTest {
    private lateinit var db: AppDatabase
    private lateinit var stats: StatsRepo

    @Before fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).allowMainThreadQueries().build()
        stats = StatsRepo(db.sessionDao(), db.taskDao())
    }
    @After fun tearDown() = db.close()

    @Test fun byTaskAggregatesAndGroupsNull() = runTest {
        val t1 = db.taskDao().insert(Task(title = "数学", createdAt = 0L))
        val now = System.currentTimeMillis()
        db.sessionDao().insert(PomodoroSession(presetId = 1, taskId = t1, startedAt = now, endedAt = now, type = "focus", plannedMin = 25, completedMin = 25, finishedByTime = true))
        db.sessionDao().insert(PomodoroSession(presetId = 1, taskId = t1, startedAt = now, endedAt = now, type = "focus", plannedMin = 25, completedMin = 25, finishedByTime = true))
        db.sessionDao().insert(PomodoroSession(presetId = 1, taskId = null, startedAt = now, endedAt = now, type = "focus", plannedMin = 25, completedMin = 25, finishedByTime = true))
        val rows = stats.byTask(0, now + 1000)
        val math = rows.first { it.taskId == t1 }
        assertEquals(50, math.focusMin); assertEquals(2, math.pomodoros)
        val none = rows.first { it.taskId == null }
        assertEquals("未指定", none.taskName)
    }

    @Test fun byHourBuckets() = runTest {
        val now = System.currentTimeMillis()
        db.sessionDao().insert(PomodoroSession(presetId = 1, taskId = null, startedAt = now, endedAt = now, type = "focus", plannedMin = 25, completedMin = 25, finishedByTime = true))
        val hours = stats.byHour(0, now + 1000)
        val curHour = java.util.Calendar.getInstance().apply { timeInMillis = now }.get(java.util.Calendar.HOUR_OF_DAY)
        assertEquals(25, hours[curHour].focusMin)
    }
}

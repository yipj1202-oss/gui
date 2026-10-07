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
import com.gui.tomato.data.db.AppDatabase
import com.gui.tomato.data.entities.Task

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class TaskDaoTest {
    private lateinit var db: AppDatabase

    @Before fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries().build()
    }
    @After fun tearDown() = db.close()

    @Test fun taskCrud() = runTest {
        val id = db.taskDao().insert(Task(title = "数学", createdAt = 1000L))
        assertEquals("数学", db.taskDao().getById(id)?.title)
        db.taskDao().update(db.taskDao().getById(id)!!.copy(completed = true, completedAt = 2000L))
        assertTrue(db.taskDao().getById(id)!!.completed)
        db.taskDao().delete(id)
        assertNull(db.taskDao().getById(id))
    }
}

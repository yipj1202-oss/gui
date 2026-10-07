package com.gui.tomato.data.dao

import androidx.room.*
import com.gui.tomato.data.entities.Task
import com.gui.tomato.data.entities.TaskImage

@Dao
interface TaskDao {
    @Insert suspend fun insert(task: Task): Long
    @Insert suspend fun insertAll(images: List<TaskImage>)
    @Update suspend fun update(task: Task)
    @Query("DELETE FROM tasks WHERE id = :id") suspend fun delete(id: Long)
    @Query("SELECT * FROM tasks WHERE id = :id") suspend fun getById(id: Long): Task?
    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY createdAt DESC") fun activeFlow(): kotlinx.coroutines.flow.Flow<List<Task>>
    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY completedAt DESC") fun doneFlow(): kotlinx.coroutines.flow.Flow<List<Task>>
    @Query("SELECT * FROM task_images WHERE taskId = :taskId ORDER BY idx") fun imagesForFlow(taskId: Long): kotlinx.coroutines.flow.Flow<List<TaskImage>>
    @Query("DELETE FROM task_images WHERE taskId = :taskId") suspend fun deleteImagesForTask(taskId: Long)
}

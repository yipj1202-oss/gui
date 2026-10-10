package com.gui.tomato.data.dao

import androidx.room.*
import com.gui.tomato.data.entities.PomodoroSession

@Dao
interface SessionDao {
    @Insert suspend fun insert(s: PomodoroSession): Long
    @Query("SELECT * FROM sessions WHERE endedAt IS NULL LIMIT 1") suspend fun getOpen(): PomodoroSession?
    @Query("UPDATE sessions SET endedAt = :endedAt, completedMin = :completedMin, finishedByTime = :ftb WHERE id = :id")
    suspend fun close(id: Long, endedAt: Long, completedMin: Int, ftb: Boolean)
    @Query("DELETE FROM sessions WHERE endedAt IS NULL") suspend fun clearOpen()
    @Query("SELECT * FROM sessions WHERE type='focus' AND startedAt BETWEEN :from AND :to") suspend fun focusInRange(from: Long, to: Long): List<PomodoroSession>
    @Query("SELECT * FROM sessions") suspend fun all(): List<PomodoroSession>

    // ── 统计删除：只删已结束的 focus session，不动正在进行的 ──
    @Query("DELETE FROM sessions WHERE type='focus' AND endedAt IS NOT NULL AND taskId = :taskId")
    suspend fun deleteStatsByTask(taskId: Long)

    @Query("DELETE FROM sessions WHERE type='focus' AND endedAt IS NOT NULL AND taskId IS NULL")
    suspend fun deleteStatsForNullTask()
}
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
}

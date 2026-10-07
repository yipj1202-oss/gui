package com.gui.tomato.data.dao

import androidx.room.*
import com.gui.tomato.data.entities.PomodoroPreset

@Dao
interface PresetDao {
    @Insert suspend fun insert(p: PomodoroPreset): Long
    @Update suspend fun update(p: PomodoroPreset)
    @Query("DELETE FROM presets WHERE id = :id") suspend fun delete(id: Long)
    @Query("SELECT * FROM presets WHERE isDefault = 1 LIMIT 1") suspend fun getDefault(): PomodoroPreset?
    @Query("SELECT * FROM presets ORDER BY name") suspend fun listAll(): List<PomodoroPreset>
    @Query("UPDATE presets SET isDefault = 0") suspend fun clearDefault()
    @Query("UPDATE presets SET isDefault = 1 WHERE id = :id") suspend fun setDefault(id: Long)
}

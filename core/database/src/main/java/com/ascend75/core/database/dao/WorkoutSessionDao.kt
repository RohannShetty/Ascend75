package com.ascend75.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ascend75.core.database.entities.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE task_entry_id = :taskEntryId ORDER BY finished_at DESC LIMIT 1")
    suspend fun getLatestForTask(taskEntryId: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE task_entry_id = :taskEntryId ORDER BY finished_at DESC")
    fun observeForTask(taskEntryId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY finished_at ASC")
    suspend fun getAll(): List<WorkoutSessionEntity>

    @Query("DELETE FROM workout_sessions")
    suspend fun clearAll()
}

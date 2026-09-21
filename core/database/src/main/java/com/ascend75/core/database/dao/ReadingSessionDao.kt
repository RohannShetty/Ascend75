package com.ascend75.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ascend75.core.database.entities.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ReadingSessionEntity)

    @Query("SELECT * FROM reading_sessions WHERE task_entry_id = :taskEntryId ORDER BY logged_at DESC")
    fun observeForTask(taskEntryId: String): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions WHERE task_entry_id = :taskEntryId ORDER BY logged_at DESC LIMIT 1")
    suspend fun getLatestForTask(taskEntryId: String): ReadingSessionEntity?

    @Query("SELECT * FROM reading_sessions ORDER BY logged_at ASC")
    suspend fun getAll(): List<ReadingSessionEntity>

    @Query("DELETE FROM reading_sessions")
    suspend fun clearAll()
}

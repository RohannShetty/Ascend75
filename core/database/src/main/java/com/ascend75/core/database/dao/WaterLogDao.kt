package com.ascend75.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ascend75.core.database.entities.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: WaterLogEntity)

    @Query("SELECT * FROM water_logs WHERE task_entry_id = :taskEntryId ORDER BY logged_at DESC")
    fun observeForTask(taskEntryId: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE task_entry_id = :taskEntryId AND logged_at >= :since ORDER BY logged_at ASC")
    suspend fun getSince(taskEntryId: String, since: Long): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs ORDER BY logged_at ASC")
    suspend fun getAll(): List<WaterLogEntity>

    @Query("DELETE FROM water_logs")
    suspend fun clearAll()
}

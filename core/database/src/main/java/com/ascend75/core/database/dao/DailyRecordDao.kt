package com.ascend75.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.DailyRecordWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRecord(record: DailyRecordEntity)

    @Update
    suspend fun updateDailyRecord(record: DailyRecordEntity)

    @Transaction
    @Query("SELECT * FROM daily_records WHERE challenge_instance_id = :challengeId AND day_number = :dayNumber LIMIT 1")
    fun observeDailyRecordWithTasks(challengeId: String, dayNumber: Int): Flow<DailyRecordWithTasks?>

    @Transaction
    @Query("SELECT * FROM daily_records WHERE challenge_instance_id = :challengeId AND day_number = :dayNumber LIMIT 1")
    suspend fun getDailyRecordWithTasks(challengeId: String, dayNumber: Int): DailyRecordWithTasks?

    @Query("SELECT * FROM daily_records WHERE challenge_instance_id = :challengeId ORDER BY day_number ASC")
    fun observeDailyRecordsForChallenge(challengeId: String): Flow<List<DailyRecordEntity>>

    @Query("SELECT MAX(day_number) FROM daily_records WHERE challenge_instance_id = :challengeId")
    suspend fun getLatestDayNumber(challengeId: String): Int?

    @Query("DELETE FROM daily_records")
    suspend fun clearAllDailyRecords()
}

package com.ascend75.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ascend75.core.database.entities.ChallengeInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeInstanceEntity)

    @Update
    suspend fun updateChallenge(challenge: ChallengeInstanceEntity)

    @Query("SELECT * FROM challenge_instances WHERE status = 'ACTIVE' LIMIT 1")
    fun observeActiveChallenge(): Flow<ChallengeInstanceEntity?>

    @Query("SELECT * FROM challenge_instances WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveChallenge(): ChallengeInstanceEntity?

    @Query("SELECT * FROM challenge_instances ORDER BY attempt_number DESC")
    fun observeAllChallenges(): Flow<List<ChallengeInstanceEntity>>

    @Query("SELECT * FROM challenge_instances WHERE id = :id")
    suspend fun getChallengeById(id: String): ChallengeInstanceEntity?

    @Query("DELETE FROM challenge_instances WHERE id = :id")
    suspend fun deleteChallenge(id: String)

    @Query("DELETE FROM challenge_instances")
    suspend fun clearAllChallenges()
}

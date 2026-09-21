package com.ascend75.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ascend75.core.database.entities.ProgressPhotoEntity
import kotlinx.coroutines.flow.Flow

/** Day-scoped view of a vault entry, resolved across the task entry that owns the photo. */
data class VaultPhotoProjection(
    val dayNumber: Int,
    val encryptedFilePath: String,
    val capturedAt: Long
)

@Dao
interface ProgressPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: ProgressPhotoEntity)

    @Query("SELECT COUNT(*) FROM progress_photos")
    suspend fun getCount(): Int

    @Query(
        """
        SELECT d.day_number AS dayNumber,
               p.encrypted_file_path AS encryptedFilePath,
               p.captured_at AS capturedAt
        FROM progress_photos p
        JOIN task_entries t ON t.id = p.task_entry_id
        JOIN daily_records d ON d.id = t.daily_record_id
        ORDER BY d.day_number DESC
        """
    )
    fun observeVaultPhotos(): Flow<List<VaultPhotoProjection>>

    @Query("SELECT * FROM progress_photos ORDER BY captured_at ASC")
    suspend fun getAll(): List<ProgressPhotoEntity>

    @Query("DELETE FROM progress_photos")
    suspend fun clearAll()
}

package com.ascend75.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress_photos",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_entry_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["task_entry_id"])
    ]
)
data class ProgressPhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "task_entry_id")
    val taskEntryId: String,

    @ColumnInfo(name = "encrypted_file_path")
    val encryptedFilePath: String,

    @ColumnInfo(name = "photo_hash")
    val photoHash: String,

    @ColumnInfo(name = "file_size_bytes")
    val fileSizeBytes: Long,

    @ColumnInfo(name = "captured_at")
    val capturedAt: Long = System.currentTimeMillis()
)

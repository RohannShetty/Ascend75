package com.ascend75.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks a single day (1..75) of an active challenge instance.
 */
@Entity(
    tableName = "daily_records",
    foreignKeys = [
        ForeignKey(
            entity = ChallengeInstanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["challenge_instance_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["challenge_instance_id", "day_number"], unique = true)
    ]
)
data class DailyRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "challenge_instance_id")
    val challengeInstanceId: String,

    @ColumnInfo(name = "day_number")
    val dayNumber: Int,

    @ColumnInfo(name = "calendar_date")
    val calendarDate: Long, // Epoch Day

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "sleep_cutoff_timestamp")
    val sleepCutoffTimestamp: Long,

    @ColumnInfo(name = "reflection_notes")
    val reflectionNotes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

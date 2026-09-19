package com.ascend75.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks an individual habit requirement for a given daily record.
 */
@Entity(
    tableName = "task_entries",
    foreignKeys = [
        ForeignKey(
            entity = DailyRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["daily_record_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["daily_record_id"])
    ]
)
data class TaskEntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "daily_record_id")
    val dailyRecordId: String,

    @ColumnInfo(name = "habit_type")
    val habitType: String, // "DIET", "WORKOUT_1", "WORKOUT_2", "WATER", "READING", "PHOTO", "CUSTOM"

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "target_value")
    val targetValue: Double = 1.0,

    @ColumnInfo(name = "current_value")
    val currentValue: Double = 0.0
)

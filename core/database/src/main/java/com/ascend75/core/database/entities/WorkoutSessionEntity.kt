package com.ascend75.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
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
data class WorkoutSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "task_entry_id")
    val taskEntryId: String,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,

    @ColumnInfo(name = "is_outdoor")
    val isOutdoor: Boolean,

    @ColumnInfo(name = "workout_type")
    val workoutType: String, // "RUNNING", "WALKING", "STRENGTH", "HIIT", "YOGA", "OTHER"

    @ColumnInfo(name = "intensity")
    val intensity: String, // "LOW", "MODERATE", "HIGH"

    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @ColumnInfo(name = "finished_at")
    val finishedAt: Long,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)

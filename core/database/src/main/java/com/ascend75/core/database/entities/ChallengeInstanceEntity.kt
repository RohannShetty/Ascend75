package com.ascend75.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a discrete 75-day challenge attempt.
 * Preserves history when an attempt is reset in Strict Mode.
 */
@Entity(tableName = "challenge_instances")
data class ChallengeInstanceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "attempt_number")
    val attemptNumber: Int,

    @ColumnInfo(name = "mode")
    val mode: String, // "STRICT_75", "FLEXIBLE_75", "SOFT_75", "CUSTOM"

    @ColumnInfo(name = "status")
    val status: String, // "ACTIVE", "COMPLETED", "RESET_ARCHIVED"

    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @ColumnInfo(name = "ended_at")
    val endedAt: Long? = null,

    @ColumnInfo(name = "config_json")
    val configJson: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

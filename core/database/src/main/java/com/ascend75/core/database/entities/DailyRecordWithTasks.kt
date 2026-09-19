package com.ascend75.core.database.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Composite relational data class combining a daily record with all its task entries.
 */
data class DailyRecordWithTasks(
    @Embedded
    val dailyRecord: DailyRecordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "daily_record_id"
    )
    val tasks: List<TaskEntryEntity>
)

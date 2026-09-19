package com.ascend75.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "water_logs",
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
data class WaterLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "task_entry_id")
    val taskEntryId: String,

    @ColumnInfo(name = "amount_ml")
    val amountMl: Int,

    @ColumnInfo(name = "logged_at")
    val loggedAt: Long = System.currentTimeMillis()
)

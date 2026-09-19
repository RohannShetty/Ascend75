package com.ascend75.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_sessions",
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
data class ReadingSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "task_entry_id")
    val taskEntryId: String,

    @ColumnInfo(name = "book_title")
    val bookTitle: String,

    @ColumnInfo(name = "book_author")
    val bookAuthor: String? = null,

    @ColumnInfo(name = "start_page")
    val startPage: Int,

    @ColumnInfo(name = "end_page")
    val endPage: Int,

    @ColumnInfo(name = "pages_read")
    val pagesRead: Int,

    @ColumnInfo(name = "reading_duration_seconds")
    val readingDurationSeconds: Int? = null,

    @ColumnInfo(name = "key_takeaway")
    val keyTakeaway: String? = null,

    @ColumnInfo(name = "logged_at")
    val loggedAt: Long = System.currentTimeMillis()
)

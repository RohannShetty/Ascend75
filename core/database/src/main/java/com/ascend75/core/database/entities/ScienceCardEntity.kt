package com.ascend75.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "science_cards")
data class ScienceCardEntity(
    @PrimaryKey
    @ColumnInfo(name = "day_number")
    val dayNumber: Int,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "category")
    val category: String, // "CIRCADIAN", "FOCUS", "DOPAMINE", "RECOVERY", "HABITS"

    @ColumnInfo(name = "summary")
    val summary: String,

    @ColumnInfo(name = "mechanism")
    val mechanism: String,

    @ColumnInfo(name = "action_item")
    val actionItem: String,

    @ColumnInfo(name = "source_citation")
    val sourceCitation: String,

    @ColumnInfo(name = "doi_or_url")
    val doiOrUrl: String? = null,

    @ColumnInfo(name = "is_bookmarked")
    val isBookmarked: Boolean = false
)

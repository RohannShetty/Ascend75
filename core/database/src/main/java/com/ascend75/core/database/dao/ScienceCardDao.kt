package com.ascend75.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ascend75.core.database.entities.ScienceCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScienceCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<ScienceCardEntity>)

    @Query("SELECT * FROM science_cards WHERE day_number = :dayNumber LIMIT 1")
    fun observeCardByDay(dayNumber: Int): Flow<ScienceCardEntity?>

    @Query("SELECT * FROM science_cards WHERE day_number <= :unlockedUpToDay ORDER BY day_number ASC")
    fun observeUnlockedCards(unlockedUpToDay: Int): Flow<List<ScienceCardEntity>>

    @Query("SELECT * FROM science_cards WHERE day_number <= :unlockedUpToDay AND category = :category ORDER BY day_number ASC")
    fun observeUnlockedCardsByCategory(unlockedUpToDay: Int, category: String): Flow<List<ScienceCardEntity>>

    @Query("SELECT * FROM science_cards WHERE day_number <= :unlockedUpToDay AND is_bookmarked = 1 ORDER BY day_number ASC")
    fun observeBookmarkedCards(unlockedUpToDay: Int): Flow<List<ScienceCardEntity>>

    @Query("UPDATE science_cards SET is_bookmarked = :isBookmarked WHERE day_number = :dayNumber")
    suspend fun updateBookmark(dayNumber: Int, isBookmarked: Boolean)

    @Query("SELECT COUNT(*) FROM science_cards")
    suspend fun getCardCount(): Int
}

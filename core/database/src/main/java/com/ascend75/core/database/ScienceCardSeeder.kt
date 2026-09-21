package com.ascend75.core.database

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ascend75.core.database.dao.ScienceCardDao
import com.ascend75.core.database.entities.ScienceCardEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Provider

class ScienceCardSeeder(
    private val context: Context,
    private val scienceCardDaoProvider: Provider<ScienceCardDao>
) : RoomDatabase.Callback() {

    /**
     * Runs on every open rather than only on create, so a database that was seeded before the full
     * 75-card asset shipped is repaired on upgrade instead of staying permanently short.
     */
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        CoroutineScope(Dispatchers.IO).launch {
            if (scienceCardDaoProvider.get().getCardCount() < CARD_COUNT) {
                seedCards()
            }
        }
    }

    suspend fun seedCards() {
        runCatching {
            val jsonString = context.assets.open("science_cards.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            val cards = mutableListOf<ScienceCardEntity>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                cards.add(
                    ScienceCardEntity(
                        dayNumber = obj.getInt("dayNumber"),
                        title = obj.getString("title"),
                        category = obj.getString("category"),
                        summary = obj.getString("summary"),
                        mechanism = obj.getString("mechanism"),
                        actionItem = obj.getString("actionItem"),
                        sourceCitation = obj.getString("sourceCitation"),
                        doiOrUrl = if (obj.isNull("doiOrUrl")) null else obj.getString("doiOrUrl"),
                        isBookmarked = false
                    )
                )
            }

            scienceCardDaoProvider.get().insertAll(cards)
        }.onFailure { error ->
            error.printStackTrace()
        }
    }

    companion object {
        const val CARD_COUNT = 75
    }
}

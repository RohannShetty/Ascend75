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

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            seedCards()
        }
    }

    suspend fun seedCards() {
        try {
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
                        doiOrUrl = obj.optString("doiOrUrl", null),
                        isBookmarked = false
                    )
                )
            }

            scienceCardDaoProvider.get().insertAll(cards)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

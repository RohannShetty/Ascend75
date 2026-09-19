package com.ascend75.core.database

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ScienceCardSeederTest {

    @Test
    fun verifyScienceCardsJsonFileIntegrityAndCompleteness() {
        val assetFile = File("src/main/assets/science_cards.json")
        val altPath = File("core/database/src/main/assets/science_cards.json")
        val target = if (assetFile.exists()) assetFile else altPath

        if (target.exists()) {
            val content = target.readText()
            val jsonArray = JSONArray(content)

            assertEquals("Must contain exactly 75 science cards", 75, jsonArray.length())

            val validCategories = setOf("CIRCADIAN", "FOCUS", "DOPAMINE", "RECOVERY", "HABITS")

            for (i in 0 until jsonArray.length()) {
                val card = jsonArray.getJSONObject(i)
                val dayNum = card.getInt("dayNumber")
                assertEquals(i + 1, dayNum)

                val category = card.getString("category")
                assertTrue("Category $category must be valid", validCategories.contains(category))

                assertNotNull(card.getString("title"))
                assertNotNull(card.getString("summary"))
                assertNotNull(card.getString("mechanism"))
                assertNotNull(card.getString("actionItem"))
                assertNotNull(card.getString("sourceCitation"))
            }
        }
    }
}

package com.ascend75.core.database

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ScienceCardSeederTest {

    private val assetCandidates = listOf(
        File("src/main/assets/science_cards.json"),
        File("core/database/src/main/assets/science_cards.json")
    )

    @Test
    fun seededAssetCoversAllSeventyFiveDaysWithUniqueTitlesAndResolvableSources() {
        val target = assetCandidates.firstOrNull { it.exists() }
        assertTrue(
            "science_cards.json must be present for the seeder to ship content; looked in " +
                assetCandidates.joinToString { it.absolutePath },
            target != null
        )

        val jsonArray = JSONArray(target!!.readText())
        assertEquals("Must contain exactly 75 science cards", 75, jsonArray.length())

        val validCategories = setOf("CIRCADIAN", "FOCUS", "DOPAMINE", "RECOVERY", "HABITS")
        val titles = mutableSetOf<String>()
        val sources = mutableSetOf<String>()

        for (index in 0 until jsonArray.length()) {
            val card = jsonArray.getJSONObject(index)
            val day = card.getInt("dayNumber")
            assertEquals("Card $index must describe day ${index + 1}", index + 1, day)

            assertTrue(
                "Day $day has category '${card.getString("category")}' outside $validCategories",
                validCategories.contains(card.getString("category"))
            )

            for (field in listOf("title", "summary", "mechanism", "actionItem", "sourceCitation")) {
                assertTrue("Day $day must have a non-blank $field", card.getString(field).isNotBlank())
            }

            val source = card.optString("doiOrUrl")
            assertTrue("Day $day must cite a resolvable source, found '$source'", source.startsWith("http"))

            titles.add(card.getString("title"))
            sources.add(source)
        }

        assertEquals("Every card must have a distinct title", 75, titles.size)
        assertTrue("The curriculum must draw on many distinct sources", sources.size >= 60)
    }
}

package com.ascend75.feature.water

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WaterRateLimiterTest {

    @Test
    fun verifyNormalIntakeDoesNotTriggerHyponatremiaWarning() {
        val viewModel = WaterViewModel(taskEntryDao = io.mockk.mockk(relaxed = true))
        val now = 1000000L

        // Single 500ml intake
        val isRisk = viewModel.checkHyponatremiaRisk(500, now)
        assertFalse(isRisk)
    }

    @Test
    fun verifyRapidExcessiveIntakeTriggersHyponatremiaWarning() {
        val viewModel = WaterViewModel(taskEntryDao = io.mockk.mockk(relaxed = true))
        val now = 1000000L

        // Rapid intake exceeding 1200ml
        val isRisk = viewModel.checkHyponatremiaRisk(1500, now)
        assertTrue(isRisk)
    }
}

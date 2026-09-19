package com.ascend75.feature.dashboard

import com.ascend75.core.common.domain.ChallengeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardViewModelTest {

    @Test
    fun verifyDashboardInitialState() {
        val state = DashboardUiState(
            isLoading = false,
            dayNumber = 1,
            mode = ChallengeMode.STRICT_75,
            completedCount = 3,
            totalCount = 6
        )

        assertFalse(state.isLoading)
        assertEquals(1, state.dayNumber)
        assertEquals(3, state.completedCount)
        assertEquals(6, state.totalCount)
        assertEquals(ChallengeMode.STRICT_75, state.mode)
    }

    @Test
    fun verifyResetDialogConditionInStrictMode() {
        val state = DashboardUiState(
            isLoading = false,
            dayNumber = 5,
            mode = ChallengeMode.STRICT_75,
            completedCount = 4,
            totalCount = 6,
            isPastCutoff = true,
            showResetDialog = true
        )

        assertTrue(state.isPastCutoff)
        assertTrue(state.showResetDialog)
    }
}

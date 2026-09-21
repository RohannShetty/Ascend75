package com.ascend75.feature.water

import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.dao.WaterLogDao
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.database.entities.WaterLogEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WaterViewModelTest {

    private val taskEntryDao = mockk<TaskEntryDao>(relaxed = true)
    private val waterLogDao = mockk<WaterLogDao>(relaxed = true)

    private val taskId = "water-task"

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { taskEntryDao.getTaskById(taskId) } returns TaskEntryEntity(
            id = taskId,
            dailyRecordId = "record",
            habitType = "WATER",
            targetValue = 3800.0
        )
        coEvery { waterLogDao.getSince(taskId, 0L) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun warningsAreBasedOnTheCumulativeRollingHourNotTheSingleEntry() = runTest {
        val viewModel = WaterViewModel(taskEntryDao, waterLogDao)

        viewModel.logWater(500, taskId)
        assertEquals(500, viewModel.uiState.value.currentTotalMl)

        assertTrue(
            "500 + 800 crosses the 1,200 ml hourly ceiling",
            viewModel.checkHyponatremiaRisk(800)
        )
    }

    @Test
    fun exactlyTwelveHundredMillilitresInTheHourIsAllowedAndOneMoreTriggersTheWarning() = runTest {
        val viewModel = WaterViewModel(taskEntryDao, waterLogDao)
        viewModel.logWater(1200, taskId, forceLog = true)

        assertFalse("1,200 ml in one hour is at the ceiling, not over it", viewModel.checkHyponatremiaRisk(0))
        assertTrue("1,201 ml in one hour is over the ceiling", viewModel.checkHyponatremiaRisk(1))
    }

    @Test
    fun intakeOlderThanSixtyMinutesFallsOutOfTheWindow() = runTest {
        val sixtyOneMinutesAgo = System.currentTimeMillis() - 61 * 60 * 1000L
        coEvery { waterLogDao.getSince(taskId, 0L) } returns listOf(
            WaterLogEntity(
                id = "old",
                taskEntryId = taskId,
                amountMl = 1500,
                loggedAt = sixtyOneMinutesAgo
            )
        )
        val viewModel = WaterViewModel(taskEntryDao, waterLogDao)

        viewModel.initializeFrom(taskId)

        assertEquals("The persisted total is still restored", 1500, viewModel.uiState.value.currentTotalMl)
        assertFalse(
            "A 61-minute-old 1,500 ml entry must not count toward the hourly rate",
            viewModel.checkHyponatremiaRisk(1200)
        )
        assertTrue(
            "With the old entry excluded only the new amount is measured, so 1,201 still warns",
            viewModel.checkHyponatremiaRisk(1201)
        )
    }

    @Test
    fun restoreRebuildsTheTotalAndCompletionStateFromPersistedLogs() = runTest {
        val now = System.currentTimeMillis()
        coEvery { waterLogDao.getSince(taskId, 0L) } returns listOf(
            WaterLogEntity(id = "a", taskEntryId = taskId, amountMl = 2000, loggedAt = now - 120_000L),
            WaterLogEntity(id = "b", taskEntryId = taskId, amountMl = 500, loggedAt = now - 60_000L)
        )
        val viewModel = WaterViewModel(taskEntryDao, waterLogDao)

        viewModel.initializeFrom(taskId)

        val state = viewModel.uiState.value
        assertEquals(2500, state.currentTotalMl)
        assertEquals(3800, state.targetMl)
        assertEquals(2, state.logs.size)
        assertFalse(state.isCompleted)
    }

    @Test
    fun dismissingTheWarningDoesNotRecordTheIntakeButConfirmingItDoes() = runTest {
        val viewModel = WaterViewModel(taskEntryDao, waterLogDao)

        viewModel.logWater(1300, taskId)
        assertTrue("An over-rate entry raises the warning instead of being recorded", viewModel.uiState.value.showHyponatremiaWarning)
        assertEquals(0, viewModel.uiState.value.currentTotalMl)

        viewModel.dismissWarning()
        assertFalse(viewModel.uiState.value.showHyponatremiaWarning)
        assertEquals(0, viewModel.uiState.value.currentTotalMl)

        viewModel.logWater(1300, taskId)
        viewModel.confirmPendingLog(taskId)
        assertEquals(
            "Confirming replays the amount the user actually attempted",
            1300,
            viewModel.uiState.value.currentTotalMl
        )
    }
}

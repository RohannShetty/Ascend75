package com.ascend75.feature.reading

import com.ascend75.core.database.dao.ReadingSessionDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.entities.ReadingSessionEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingViewModelTest {

    private val taskEntryDao = mockk<TaskEntryDao>(relaxed = true)
    private val readingSessionDao = mockk<ReadingSessionDao>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val taskId = "reading-task"
    private var finished = false

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        finished = false
        coEvery { taskEntryDao.getTaskById(taskId) } returns TaskEntryEntity(
            id = taskId,
            dailyRecordId = "record",
            habitType = "READING",
            targetValue = 10.0
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fewerThanTenPagesIsRejectedAndNothingIsPersisted() =
        runTest(testDispatcher.scheduler) {
            val viewModel = ReadingViewModel(taskEntryDao, readingSessionDao)
            viewModel.setPages(start = 10, end = 19)

            viewModel.saveSession(taskId) { finished = true }

            assertEquals(9, viewModel.uiState.value.pagesRead)
            assertNotNull(viewModel.uiState.value.errorMessage)
            assertEquals(false, finished)
            coVerify(exactly = 0) { readingSessionDao.insert(any()) }
            coVerify(exactly = 0) { taskEntryDao.updateTaskCompletion(any(), any(), any()) }
        }

    @Test
    fun tenPagesCompletesTheTaskAndPersistsTheSession() =
        runTest(testDispatcher.scheduler) {
            val viewModel = ReadingViewModel(taskEntryDao, readingSessionDao)
            viewModel.setBookTitle("Deep Work")
            viewModel.setTakeaway("Attention is the scarce resource")
            viewModel.setPages(start = 1, end = 11)

            viewModel.saveSession(taskId) { finished = true }

            assertTrue(finished)
            assertNull(viewModel.uiState.value.errorMessage)

            val session = slot<ReadingSessionEntity>()
            coVerify { readingSessionDao.insert(capture(session)) }
            assertEquals(10, session.captured.pagesRead)
            assertEquals("Deep Work", session.captured.bookTitle)
            assertEquals("Attention is the scarce resource", session.captured.keyTakeaway)
            coVerify { taskEntryDao.updateTaskCompletion(taskId, true, any()) }
        }

    @Test
    fun theTimerTicksOncePerSecondAndFreezesWhenStopped() =
        runTest(testDispatcher.scheduler) {
            val viewModel = ReadingViewModel(taskEntryDao, readingSessionDao)

            viewModel.startTimer()
            advanceTimeBy(3_500L)
            runCurrent()
            assertEquals(3, viewModel.uiState.value.readingDurationSeconds)
            assertTrue(viewModel.uiState.value.isTimerRunning)

            viewModel.stopTimer()
            advanceTimeBy(5_000L)
            runCurrent()
            assertEquals("A stopped timer must stop accumulating", 3, viewModel.uiState.value.readingDurationSeconds)

            viewModel.setPages(start = 1, end = 11)
            viewModel.saveSession(taskId) { }

            val session = slot<ReadingSessionEntity>()
            coVerify { readingSessionDao.insert(capture(session)) }
            assertEquals(3, session.captured.readingDurationSeconds)
        }
}

package com.ascend75.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ascend75.core.common.domain.ChallengeMode
import com.ascend75.core.common.domain.ChallengeRulesEngine
import com.ascend75.core.database.AscendDatabase
import com.ascend75.core.datastore.AscendPreferencesDataSource
import com.ascend75.core.datastore.UserPreferences
import com.ascend75.feature.dashboard.data.DailyProtocolRepository
import com.ascend75.feature.dashboard.data.TodayProtocol
import com.ascend75.feature.onboarding.OnboardingViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The journey the product promises: finish onboarding, work a real day against the real schema, then
 * let the day boundary roll the challenge forward.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AscendEndToEndJourneyTest {

    private lateinit var database: AscendDatabase
    private lateinit var repository: DailyProtocolRepository
    private lateinit var preferencesDataSource: AscendPreferencesDataSource
    private val preferences = MutableStateFlow(UserPreferences())

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AscendDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        preferencesDataSource = mockk(relaxed = true)
        every { preferencesDataSource.userPreferencesFlow } returns preferences

        repository = DailyProtocolRepository(
            challengeDao = database.challengeDao(),
            dailyRecordDao = database.dailyRecordDao(),
            taskEntryDao = database.taskEntryDao(),
            preferencesDataSource = preferencesDataSource
        )

        preferences.value = UserPreferences(isOnboardingCompleted = false)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun onboardingSeedsTheChosenModesDayOneAndCompletingItAdvancesToDayTwo() = runBlocking {
        val onboarding = OnboardingViewModel(
            challengeDao = database.challengeDao(),
            dailyRecordDao = database.dailyRecordDao(),
            taskEntryDao = database.taskEntryDao(),
            preferencesDataSource = preferencesDataSource
        )
        onboarding.setDisclaimerAccepted(true)
        onboarding.setSelectedMode(ChallengeMode.SOFT_75)

        var onboardingFinished = false
        val finished = CompletableDeferred<Unit>()
        onboarding.completeOnboarding {
            onboardingFinished = true
            finished.complete(Unit)
        }

        val awaitFailure = runCatching { withTimeout(20_000L) { finished.await() } }.exceptionOrNull()
        assertNull(
            "Onboarding never finished; error=${onboarding.uiState.value.errorMessage}",
            awaitFailure
        )
        assertTrue("Onboarding must report success", onboardingFinished)
        assertEquals(null, onboarding.uiState.value.errorMessage)

        preferences.value = UserPreferences(
            isOnboardingCompleted = true,
            sleepCutoffHour = 3,
            selectedMode = ChallengeMode.SOFT_75.name
        )

        val challengeId = requireNotNull(database.challengeDao().getActiveChallenge()).id
        val dayOne = awaitProtocol()
        assertEquals(1, dayOne.record.dayNumber)
        assertEquals(ChallengeMode.SOFT_75, dayOne.mode)
        assertEquals(
            "Day 1 must materialise the selected mode's habit set",
            ChallengeRulesEngine.getTasksForMode(ChallengeMode.SOFT_75).size,
            dayOne.tasks.size
        )
        assertTrue("A fresh challenge starts with nothing completed", dayOne.tasks.none { it.isCompleted })
        assertFalse(dayOne.isPastCutoff)

        // Complete every habit for the day.
        val completedAt = System.currentTimeMillis()
        dayOne.tasks.forEach { task ->
            database.taskEntryDao().updateTaskCompletion(task.id, isCompleted = true, completedAt = completedAt)
        }

        // Cross the sleep cutoff.
        database.dailyRecordDao().updateDailyRecord(
            dayOne.record.copy(sleepCutoffTimestamp = System.currentTimeMillis() - 60_000L)
        )

        val dueToday = awaitProtocol()
        assertTrue(dueToday.isPastCutoff)
        assertEquals(dayOne.tasks.size, dueToday.tasks.count { it.isCompleted })

        repository.advanceDayIfDue(dueToday)

        val dayTwo = database.dailyRecordDao().getDailyRecordWithTasks(challengeId, 2)
        assertNotNull("Day 2 must exist after a fully completed day crosses the cutoff", dayTwo)
        assertEquals(dayOne.tasks.size, dayTwo!!.tasks.size)
        assertTrue("Day 1 must be recorded as completed", database.dailyRecordDao()
            .getDailyRecordWithTasks(challengeId, 1)!!.dailyRecord.isCompleted)

        val dayTwoProtocol = awaitProtocol()
        assertEquals(2, dayTwoProtocol.record.dayNumber)
        assertEquals("The finished day counts toward the streak", 1, dayTwoProtocol.streakDays)
    }

    private suspend fun awaitProtocol(): TodayProtocol =
        requireNotNull(repository.observeToday().first { it != null }) { "No active protocol was observed" }
}

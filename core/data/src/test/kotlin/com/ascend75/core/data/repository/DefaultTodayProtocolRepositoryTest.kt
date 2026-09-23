package com.ascend75.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ascend75.core.common.domain.ChallengeRulesEngine
import com.ascend75.core.database.AscendDatabase
import com.ascend75.core.database.entities.ChallengeInstanceEntity
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.domain.model.HabitType
import com.ascend75.core.domain.model.TodayProtocol
import com.ascend75.core.domain.model.UserPreferences
import com.ascend75.core.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
import java.time.LocalDate
import java.util.UUID

/**
 * Ported from :feature:dashboard's DailyProtocolRepositoryTest when the repository moved into
 * :core:data. Every assertion is unchanged: this is the regression net for day advancement.
 */
@RunWith(RobolectricTestRunner::class)
class DefaultTodayProtocolRepositoryTest {

    private lateinit var database: AscendDatabase
    private lateinit var repository: DefaultTodayProtocolRepository
    private lateinit var preferences: MutableStateFlow<UserPreferences>
    private lateinit var challengeId: String

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AscendDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        preferences = MutableStateFlow(
            UserPreferences(
                isOnboardingCompleted = true,
                sleepCutoffHour = 4,
                sleepCutoffMinute = 30,
                selectedMode = ChallengeMode.STRICT_75.name
            )
        )
        val settingsRepository = mockk<SettingsRepository>(relaxed = true)
        every { settingsRepository.preferences } returns preferences

        repository = DefaultTodayProtocolRepository(
            challengeDao = database.challengeDao(),
            dailyRecordDao = database.dailyRecordDao(),
            taskEntryDao = database.taskEntryDao(),
            settingsRepository = settingsRepository
        )

        challengeId = UUID.randomUUID().toString()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun completedDayPastCutoffMaterialisesTheNextDayWithTheModesTaskSet() = runBlocking {
        val cutoff = pastCutoff()
        seedChallenge()
        seedDay(dayNumber = 1, mode = ChallengeMode.STRICT_75, completedCount = 6, cutoffTimestamp = cutoff)

        val protocol = awaitProtocol()
        repository.advanceDayIfDue(protocol)

        val dayTwo = database.dailyRecordDao().getDailyRecordWithTasks(challengeId, 2)
        assertNotNull("Day 2 must exist after a completed day crosses the cutoff", dayTwo)
        assertEquals(6, dayTwo!!.tasks.size)
        assertFalse(dayTwo.dailyRecord.isCompleted)
        assertTrue(
            "Day 2 must get a new cutoff derived from the user's preference, not the previous day's",
            dayTwo.dailyRecord.sleepCutoffTimestamp > protocol.record.sleepCutoffTimestamp
        )

        val dayOne = database.dailyRecordDao().getDailyRecordWithTasks(challengeId, 1)
        assertTrue("The finished day must be marked complete", dayOne!!.dailyRecord.isCompleted)
    }

    @Test
    fun advancingCarriesCustomTargetsForwardForTheSameHabitType() = runBlocking {
        seedChallenge()
        seedDay(
            dayNumber = 1,
            mode = ChallengeMode.STRICT_75,
            completedCount = 6,
            cutoffTimestamp = pastCutoff(),
            waterTargetMl = 4200.0
        )

        repository.advanceDayIfDue(awaitProtocol())

        val dayTwo = database.dailyRecordDao().getDailyRecordWithTasks(challengeId, 2)!!
        val water = dayTwo.tasks.first { it.habitType == "WATER" }
        assertEquals(4200.0, water.targetValue, 0.001)
    }

    @Test
    fun strictModeWithIncompleteTasksPastCutoffWaitsForExplicitConsent() = runBlocking {
        seedChallenge()
        seedDay(dayNumber = 3, mode = ChallengeMode.STRICT_75, completedCount = 4, cutoffTimestamp = pastCutoff())

        repository.advanceDayIfDue(awaitProtocol())

        assertNull(
            "Strict 75 must not advance silently - DayResetDialog owns that decision",
            database.dailyRecordDao().getDailyRecordWithTasks(challengeId, 4)
        )
    }

    @Test
    fun flexibleModeWithIncompleteTasksPastCutoffAdvancesWithoutResetting() = runBlocking {
        seedChallenge(ChallengeMode.FLEXIBLE_75)
        seedDay(dayNumber = 3, mode = ChallengeMode.FLEXIBLE_75, completedCount = 2, cutoffTimestamp = pastCutoff())

        repository.advanceDayIfDue(awaitProtocol())

        val dayFour = database.dailyRecordDao().getDailyRecordWithTasks(challengeId, 4)
        assertNotNull("Flexible 75 advances incomplete days", dayFour)
        assertFalse(
            "An incomplete day must not be recorded as a completed streak day",
            database.dailyRecordDao().getDailyRecordWithTasks(challengeId, 3)!!.dailyRecord.isCompleted
        )
    }

    @Test
    fun nothingAdvancesBeforeTheSleepCutoff() = runBlocking {
        seedChallenge()
        seedDay(
            dayNumber = 1,
            mode = ChallengeMode.STRICT_75,
            completedCount = 6,
            cutoffTimestamp = System.currentTimeMillis() + 3_600_000L
        )

        val protocol = awaitProtocol()
        assertFalse(protocol.isPastCutoff)
        repository.advanceDayIfDue(protocol)

        assertNull(database.dailyRecordDao().getDailyRecordWithTasks(challengeId, 2))
    }

    @Test
    fun advancingTwiceForTheSameDayIsIdempotent() = runBlocking {
        seedChallenge()
        seedDay(dayNumber = 1, mode = ChallengeMode.STRICT_75, completedCount = 6, cutoffTimestamp = pastCutoff())

        val protocol = awaitProtocol()
        repository.advanceDayIfDue(protocol)
        repository.advanceDayIfDue(protocol)

        assertEquals(1, database.dailyRecordDao().getDailyRecordsForChallenge(challengeId).count { it.dayNumber == 2 })
    }

    @Test
    fun streakCountsConsecutiveCompletedDaysAndIgnoresTheDayInProgress() = runBlocking {
        seedChallenge(ChallengeMode.FLEXIBLE_75)
        seedDay(
            dayNumber = 1,
            mode = ChallengeMode.FLEXIBLE_75,
            completedCount = 6,
            cutoffTimestamp = pastCutoff()
        )
        database.dailyRecordDao().updateDailyRecord(
            database.dailyRecordDao().getDailyRecordWithTasks(challengeId, 1)!!.dailyRecord.copy(isCompleted = true)
        )
        seedDay(
            dayNumber = 2,
            mode = ChallengeMode.FLEXIBLE_75,
            completedCount = 1,
            cutoffTimestamp = System.currentTimeMillis() + 3_600_000L
        )

        val protocol = awaitProtocol()

        assertEquals(2, protocol.record.dayNumber)
        assertEquals("Only completed days count toward the streak", 1, protocol.streakDays)
    }

    private suspend fun awaitProtocol(): TodayProtocol =
        requireNotNull(repository.observeToday().first { it != null }) { "No active protocol was observed" }

    private fun pastCutoff(): Long = System.currentTimeMillis() - 60_000L

    private suspend fun seedChallenge(mode: ChallengeMode = ChallengeMode.STRICT_75) {
        database.challengeDao().insertChallenge(
            ChallengeInstanceEntity(
                id = challengeId,
                attemptNumber = 1,
                mode = mode.name,
                status = "ACTIVE",
                startedAt = System.currentTimeMillis() - 86_400_000L,
                configJson = "{}"
            )
        )
    }

    private suspend fun seedDay(
        dayNumber: Int,
        mode: ChallengeMode,
        completedCount: Int,
        cutoffTimestamp: Long,
        waterTargetMl: Double = 3800.0
    ): String {
        val recordId = UUID.randomUUID().toString()
        database.dailyRecordDao().insertDailyRecord(
            DailyRecordEntity(
                id = recordId,
                challengeInstanceId = challengeId,
                dayNumber = dayNumber,
                calendarDate = LocalDate.now().minusDays((10 - dayNumber).toLong()).toEpochDay(),
                isCompleted = false,
                sleepCutoffTimestamp = cutoffTimestamp
            )
        )

        val now = System.currentTimeMillis()
        database.taskEntryDao().insertTaskEntries(
            ChallengeRulesEngine.getTasksForMode(mode).mapIndexed { index, spec ->
                val completed = index < completedCount
                TaskEntryEntity(
                    id = UUID.randomUUID().toString(),
                    dailyRecordId = recordId,
                    habitType = spec.habitType.raw,
                    isCompleted = completed,
                    completedAt = if (completed) now else null,
                    targetValue = if (spec.habitType == HabitType.WATER) waterTargetMl else spec.targetValue
                )
            }
        )
        return recordId
    }
}

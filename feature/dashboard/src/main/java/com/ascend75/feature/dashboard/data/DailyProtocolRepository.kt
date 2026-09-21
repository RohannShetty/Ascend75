package com.ascend75.feature.dashboard.data

import com.ascend75.core.common.domain.ChallengeMode
import com.ascend75.core.common.domain.ChallengeRulesEngine
import com.ascend75.core.common.domain.DayBoundaryEvaluator
import com.ascend75.core.common.domain.DayTransitionResult
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.entities.ChallengeInstanceEntity
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.datastore.AscendPreferencesDataSource
import com.ascend75.core.datastore.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Everything the UI needs about the challenge day currently in progress. */
data class TodayProtocol(
    val challenge: ChallengeInstanceEntity,
    val record: DailyRecordEntity,
    val tasks: List<TaskEntryEntity>,
    val prefs: UserPreferences,
    val mode: ChallengeMode,
    val isPastCutoff: Boolean,
    val streakDays: Int
)

/**
 * Single owner of the "today" protocol flow and of day advancement.
 *
 * Every screen that needs the current day observes [observeToday] instead of building its own
 * combine/collect chain, so exactly one Room observer tracks the day and two screens cannot race
 * the same day transition.
 */
@Singleton
class DailyProtocolRepository @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val dailyRecordDao: DailyRecordDao,
    private val taskEntryDao: TaskEntryDao,
    private val preferencesDataSource: AscendPreferencesDataSource
) {

    private val advanceMutex = Mutex()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeToday(): Flow<TodayProtocol?> =
        combine(
            challengeDao.observeActiveChallenge(),
            preferencesDataSource.userPreferencesFlow
        ) { challenge, prefs -> challenge to prefs }
            .flatMapLatest { (challenge, prefs) ->
                if (challenge == null) {
                    flowOf(null)
                } else {
                    dailyRecordDao.observeDailyRecordsForChallenge(challenge.id)
                        .flatMapLatest { records ->
                            val latest = records.maxByOrNull { it.dayNumber }
                            if (latest == null) {
                                flowOf(null)
                            } else {
                                val streak = consecutiveCompletedDays(records)
                                dailyRecordDao.observeDailyRecordWithTasks(challenge.id, latest.dayNumber)
                                    .map { recordWithTasks ->
                                        recordWithTasks?.let { record ->
                                            TodayProtocol(
                                                challenge = challenge,
                                                record = record.dailyRecord,
                                                tasks = record.tasks,
                                                prefs = prefs,
                                                mode = ChallengeMode.fromString(challenge.mode),
                                                isPastCutoff = DayBoundaryEvaluator.isPastSleepCutoff(
                                                    currentTimestamp = System.currentTimeMillis(),
                                                    sleepCutoffTimestamp = record.dailyRecord.sleepCutoffTimestamp
                                                ),
                                                streakDays = streak
                                            )
                                        }
                                    }
                            }
                        }
                }
            }

    /**
     * Counts the consecutive completed days ending at the most recent day. The newest record is
     * skipped when it is still in progress, otherwise a running streak would read 0 all day.
     */
    private fun consecutiveCompletedDays(records: List<DailyRecordEntity>): Int {
        val ordered = records.sortedBy { it.dayNumber }
        var streak = 0
        for (index in ordered.indices.reversed()) {
            val record = ordered[index]
            if (record.isCompleted) {
                streak++
            } else if (index != ordered.lastIndex) {
                break
            }
        }
        return streak
    }

    /**
     * Rolls the challenge forward when the day in [protocol] is due.
     *
     * Idempotent: the next day is only materialised when it does not already exist, so repeat
     * emissions of the same protocol cannot create duplicate days.
     */
    suspend fun advanceDayIfDue(protocol: TodayProtocol) {
        advanceMutex.withLock {
            val transition = ChallengeRulesEngine.evaluateDayTransition(
                currentDayNumber = protocol.record.dayNumber,
                totalTasks = protocol.tasks.size,
                completedTasks = protocol.tasks.count { it.isCompleted },
                isPastCutoff = protocol.isPastCutoff,
                mode = protocol.mode
            )

            when (transition) {
                is DayTransitionResult.CompletedAdvance ->
                    materialiseNextDay(protocol, transition.nextDayNumber)

                is DayTransitionResult.IncompleteAdvance ->
                    materialiseNextDay(protocol, transition.nextDayNumber)

                DayTransitionResult.ChallengeFinished ->
                    challengeDao.updateChallenge(
                        protocol.challenge.copy(
                            status = "COMPLETED",
                            endedAt = System.currentTimeMillis()
                        )
                    )

                // Strict 75 needs explicit consent — DayResetDialog then archiveAndResetStrictAttempt.
                is DayTransitionResult.StrictResetRequired -> Unit
                DayTransitionResult.Ongoing -> Unit
            }
        }
    }

    private suspend fun materialiseNextDay(protocol: TodayProtocol, nextDayNumber: Int) {
        val challengeId = protocol.challenge.id
        if (dailyRecordDao.getDailyRecordWithTasks(challengeId, nextDayNumber) != null) return

        if (!protocol.record.isCompleted) {
            val fullyCompleted = protocol.tasks.isNotEmpty() && protocol.tasks.all { it.isCompleted }
            dailyRecordDao.updateDailyRecord(protocol.record.copy(isCompleted = fullyCompleted))
        }

        val today = LocalDate.now()
        val nextRecordId = UUID.randomUUID().toString()
        dailyRecordDao.insertDailyRecord(
            DailyRecordEntity(
                id = nextRecordId,
                challengeInstanceId = challengeId,
                dayNumber = nextDayNumber,
                calendarDate = today.plusDays(1).toEpochDay(),
                isCompleted = false,
                sleepCutoffTimestamp = DayBoundaryEvaluator.calculateSleepCutoffTimestamp(
                    calendarDate = today,
                    sleepCutoffTime = LocalTime.of(protocol.prefs.sleepCutoffHour, protocol.prefs.sleepCutoffMinute)
                )
            )
        )

        // Carry custom water / reading targets forward for the same habit type.
        val carriedTargets = protocol.tasks.associate { it.habitType to it.targetValue }
        val taskEntities = ChallengeRulesEngine.getTasksForMode(protocol.mode).map { spec ->
            TaskEntryEntity(
                id = UUID.randomUUID().toString(),
                dailyRecordId = nextRecordId,
                habitType = spec.habitType,
                isCompleted = false,
                targetValue = carriedTargets[spec.habitType] ?: spec.targetValue
            )
        }
        taskEntryDao.insertTaskEntries(taskEntities)
    }
}

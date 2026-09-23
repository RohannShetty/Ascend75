package com.ascend75.core.data.repository

import com.ascend75.core.common.domain.ChallengeRulesEngine
import com.ascend75.core.common.domain.DayBoundaryEvaluator
import com.ascend75.core.common.domain.DayTransitionResult
import com.ascend75.core.data.mapper.toDomain
import com.ascend75.core.data.mapper.toEntity
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.domain.model.ChallengeStatus
import com.ascend75.core.domain.model.TodayProtocol
import com.ascend75.core.domain.repository.SettingsRepository
import com.ascend75.core.domain.repository.TodayProtocolRepository
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

/**
 * Single owner of the "today" protocol flow and of day advancement.
 *
 * Every screen that needs the current day observes [observeToday] instead of building its own
 * combine/collect chain, so exactly one Room observer tracks the day and two screens cannot race
 * the same day transition. Lives in :core:data because it spans four tables.
 */
@Singleton
class DefaultTodayProtocolRepository @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val dailyRecordDao: DailyRecordDao,
    private val taskEntryDao: TaskEntryDao,
    private val settingsRepository: SettingsRepository
) : TodayProtocolRepository {

    private val advanceMutex = Mutex()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeToday(): Flow<TodayProtocol?> =
        combine(
            challengeDao.observeActiveChallenge(),
            settingsRepository.preferences
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
                                                challenge = challenge.toDomain(),
                                                record = record.dailyRecord.toDomain(),
                                                tasks = record.tasks.map { it.toDomain() },
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
    override suspend fun advanceDayIfDue(protocol: TodayProtocol) {
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
                            status = ChallengeStatus.COMPLETED,
                            endedAt = System.currentTimeMillis()
                        ).toEntity()
                    )

                // Strict 75 needs explicit consent -- DayResetDialog then archiveAndRestart.
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
            val record = dailyRecordDao.getDailyRecordsForChallenge(challengeId)
                .firstOrNull { it.dayNumber == protocol.record.dayNumber }
            if (record != null) {
                dailyRecordDao.updateDailyRecord(record.copy(isCompleted = fullyCompleted))
            }
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
                habitType = spec.habitType.raw,
                isCompleted = false,
                targetValue = carriedTargets[spec.habitType] ?: spec.targetValue
            )
        }
        taskEntryDao.insertTaskEntries(taskEntities)
    }
}

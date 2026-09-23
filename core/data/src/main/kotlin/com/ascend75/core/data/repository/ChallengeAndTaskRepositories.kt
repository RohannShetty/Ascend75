package com.ascend75.core.data.repository

import androidx.room.withTransaction
import com.ascend75.core.common.domain.ChallengeRulesEngine
import com.ascend75.core.common.domain.DayBoundaryEvaluator
import com.ascend75.core.data.mapper.toDomain
import com.ascend75.core.data.mapper.toEntity
import com.ascend75.core.database.AscendDatabase
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.entities.ChallengeInstanceEntity
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.domain.model.ChallengeInstance
import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.domain.model.TaskEntry
import com.ascend75.core.domain.repository.ChallengeRepository
import com.ascend75.core.domain.repository.SettingsRepository
import com.ascend75.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTaskRepository @Inject constructor(
    private val taskEntryDao: TaskEntryDao
) : TaskRepository {

    override suspend fun getTask(id: String): TaskEntry? = taskEntryDao.getTaskById(id)?.toDomain()

    override fun observeTasksForDailyRecord(dailyRecordId: String): Flow<List<TaskEntry>> =
        taskEntryDao.observeTasksForDailyRecord(dailyRecordId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun setCompletion(taskId: String, isCompleted: Boolean, completedAt: Long?) =
        taskEntryDao.updateTaskCompletion(taskId, isCompleted, completedAt)

    override suspend fun setCurrentValue(taskId: String, currentValue: Double) =
        taskEntryDao.updateTaskCurrentValue(taskId, currentValue)

    override suspend fun saveNotes(taskId: String, notes: String) {
        val existing = taskEntryDao.getTaskById(taskId) ?: return
        taskEntryDao.updateTaskEntry(existing.copy(notes = notes))
    }

    override suspend fun insertTasks(tasks: List<TaskEntry>) =
        taskEntryDao.insertTaskEntries(tasks.map { it.toEntity() })

    override suspend fun updateTask(task: TaskEntry) = taskEntryDao.updateTaskEntry(task.toEntity())

    override suspend fun workoutTasksOnSameDay(taskId: String): List<TaskEntry> =
        taskEntryDao.getWorkoutTasksForSameDay(taskId).map { it.toDomain() }

    override suspend fun clearAll() = taskEntryDao.clearAllTasks()
}

@Singleton
class DefaultChallengeRepository @Inject constructor(
    private val database: AscendDatabase,
    private val challengeDao: ChallengeDao,
    private val dailyRecordDao: DailyRecordDao,
    private val taskEntryDao: TaskEntryDao,
    private val settingsRepository: SettingsRepository
) : ChallengeRepository {

    override fun observeActiveChallenge(): Flow<ChallengeInstance?> =
        challengeDao.observeActiveChallenge().map { it?.toDomain() }

    override suspend fun getActiveChallenge(): ChallengeInstance? = challengeDao.getActiveChallenge()?.toDomain()

    override suspend fun getChallenge(id: String): ChallengeInstance? = challengeDao.getChallengeById(id)?.toDomain()

    override suspend fun startAttempt(
        mode: String,
        configJson: String,
        sleepCutoffHour: Int,
        sleepCutoffMinute: Int,
        reflectionNotes: String?
    ): String {
        val challengeId = UUID.randomUUID().toString()
        val recordId = UUID.randomUUID().toString()
        val today = LocalDate.now()
        val challengeMode = ChallengeMode.fromString(mode)

        // One transaction: a process death can no longer leave an active challenge with no Day 1.
        database.withTransaction {
            challengeDao.insertChallenge(
                ChallengeInstanceEntity(
                    id = challengeId,
                    attemptNumber = (challengeDao.getActiveChallenge()?.attemptNumber ?: 0) + 1,
                    mode = challengeMode.name,
                    status = "ACTIVE",
                    startedAt = System.currentTimeMillis(),
                    configJson = configJson
                )
            )
            dailyRecordDao.insertDailyRecord(
                DailyRecordEntity(
                    id = recordId,
                    challengeInstanceId = challengeId,
                    dayNumber = 1,
                    calendarDate = today.toEpochDay(),
                    isCompleted = false,
                    sleepCutoffTimestamp = DayBoundaryEvaluator.calculateSleepCutoffTimestamp(
                        calendarDate = today,
                        sleepCutoffTime = LocalTime.of(sleepCutoffHour, sleepCutoffMinute)
                    ),
                    reflectionNotes = reflectionNotes
                )
            )
            taskEntryDao.insertTaskEntries(
                ChallengeRulesEngine.getTasksForMode(challengeMode).map { spec ->
                    TaskEntryEntity(
                        id = UUID.randomUUID().toString(),
                        dailyRecordId = recordId,
                        habitType = spec.habitType.raw,
                        isCompleted = false,
                        targetValue = spec.targetValue
                    )
                }
            )
        }

        settingsRepository.setActiveChallengeId(challengeId)
        settingsRepository.setSelectedMode(challengeMode.name)
        settingsRepository.setOnboardingCompleted(true)
        return challengeId
    }

    override suspend fun archiveAndRestart(
        reflectionNote: String,
        newMode: String,
        sleepCutoffHour: Int,
        sleepCutoffMinute: Int
    ): String {
        val active = challengeDao.getActiveChallenge()
        if (active != null) {
            challengeDao.updateChallenge(
                active.copy(status = "RESET_ARCHIVED", endedAt = System.currentTimeMillis())
            )
        }
        return startAttempt(
            mode = newMode,
            configJson = active?.configJson ?: "{}",
            sleepCutoffHour = sleepCutoffHour,
            sleepCutoffMinute = sleepCutoffMinute,
            reflectionNotes = reflectionNote
        )
    }

    override suspend fun updateChallenge(challenge: ChallengeInstance) =
        challengeDao.updateChallenge(challenge.toEntity())

    override suspend fun clearAll() = challengeDao.clearAllChallenges()
}

package com.ascend75.core.domain.repository

import com.ascend75.core.domain.model.ChallengeInstance
import com.ascend75.core.domain.model.DailyRecord
import com.ascend75.core.domain.model.ReadingSession
import com.ascend75.core.domain.model.ScienceCard
import com.ascend75.core.domain.model.TaskEntry
import com.ascend75.core.domain.model.TodayProtocol
import com.ascend75.core.domain.model.UserPreferences
import com.ascend75.core.domain.model.VaultPhoto
import com.ascend75.core.domain.model.VaultPhotoRecord
import com.ascend75.core.domain.model.WaterLog
import com.ascend75.core.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setActiveChallengeId(id: String?)
    suspend fun setSleepCutoff(hour: Int, minute: Int)
    suspend fun setSelectedMode(mode: String)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setLastCelebratedDay(day: Int)
    suspend fun clearAll()
}

interface ChallengeRepository {
    fun observeActiveChallenge(): Flow<ChallengeInstance?>
    suspend fun getActiveChallenge(): ChallengeInstance?
    suspend fun getChallenge(id: String): ChallengeInstance?

    /** Starts Day 1 of a brand-new attempt (used by onboarding and by the Strict reset). */
    suspend fun startAttempt(
        mode: String,
        configJson: String,
        sleepCutoffHour: Int,
        sleepCutoffMinute: Int,
        reflectionNotes: String? = null
    ): String

    /** Archives the active attempt and starts attempt #N+1 atomically. Returns the new challenge id. */
    suspend fun archiveAndRestart(
        reflectionNote: String,
        newMode: String,
        sleepCutoffHour: Int,
        sleepCutoffMinute: Int
    ): String

    suspend fun updateChallenge(challenge: ChallengeInstance)
    suspend fun clearAll()
}

interface TaskRepository {
    suspend fun getTask(id: String): TaskEntry?
    fun observeTasksForDailyRecord(dailyRecordId: String): Flow<List<TaskEntry>>
    suspend fun setCompletion(taskId: String, isCompleted: Boolean, completedAt: Long?)
    suspend fun setCurrentValue(taskId: String, currentValue: Double)
    suspend fun saveNotes(taskId: String, notes: String)
    suspend fun insertTasks(tasks: List<TaskEntry>)
    suspend fun updateTask(task: TaskEntry)

    /** All WORKOUT% tasks belonging to the same day as [taskId] -- the rest-separation advisory needs this. */
    suspend fun workoutTasksOnSameDay(taskId: String): List<TaskEntry>
    suspend fun clearAll()
}

interface TodayProtocolRepository {
    fun observeToday(): Flow<TodayProtocol?>
    suspend fun advanceDayIfDue(protocol: TodayProtocol)
}

interface WaterRepository {
    suspend fun logsForTask(taskId: String): List<WaterLog>
    fun observeLogsForTask(taskId: String): Flow<List<WaterLog>>
    suspend fun addLog(id: String, taskEntryId: String, amountMl: Int, loggedAt: Long)
    suspend fun allLogs(): List<WaterLog>
    suspend fun clearAll()
}

interface ReadingRepository {
    suspend fun latestForTask(taskId: String): ReadingSession?
    fun observeSessionsForTask(taskId: String): Flow<List<ReadingSession>>
    suspend fun addSession(session: ReadingSession)
    suspend fun allSessions(): List<ReadingSession>
    suspend fun clearAll()
}

interface WorkoutRepository {
    suspend fun latestForTask(taskId: String): WorkoutSession?
    fun observeSessionsForTask(taskId: String): Flow<List<WorkoutSession>>
    suspend fun addSession(session: WorkoutSession)
    suspend fun allSessions(): List<WorkoutSession>
    suspend fun clearAll()
}

interface VaultRepository {
    fun observeVaultPhotos(): Flow<List<VaultPhoto>>
    suspend fun photoCount(): Int
    suspend fun addPhoto(
        id: String,
        taskEntryId: String,
        encryptedFilePath: String,
        photoHash: String,
        fileSizeBytes: Long,
        capturedAt: Long
    )

    suspend fun allPhotoRecords(): List<VaultPhotoRecord>
    suspend fun clearAll()
}

interface ScienceRepository {
    fun observeCardByDay(dayNumber: Int): Flow<ScienceCard?>
    fun observeUnlockedCards(unlockedUpToDay: Int): Flow<List<ScienceCard>>
    fun observeUnlockedCardsByCategory(unlockedUpToDay: Int, category: String): Flow<List<ScienceCard>>
    fun observeBookmarkedCards(unlockedUpToDay: Int): Flow<List<ScienceCard>>
    suspend fun setBookmarked(dayNumber: Int, isBookmarked: Boolean)
    suspend fun cardCount(): Int
}

/** Day bookkeeping the dashboard/learn/settings screens need without touching DailyRecordDao. */
interface DailyRecordRepository {
    fun observeRecordsForChallenge(challengeId: String): Flow<List<DailyRecord>>
    suspend fun recordsForChallenge(challengeId: String): List<DailyRecord>
    suspend fun tasksForDay(challengeId: String, dayNumber: Int): List<TaskEntry>
    suspend fun latestDayNumber(challengeId: String): Int?
    suspend fun clearAll()
}

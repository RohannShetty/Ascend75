package com.ascend75.core.data.repository

import com.ascend75.core.data.mapper.toDomain
import com.ascend75.core.data.mapper.toEntity
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.ProgressPhotoDao
import com.ascend75.core.database.dao.ReadingSessionDao
import com.ascend75.core.database.dao.ScienceCardDao
import com.ascend75.core.database.dao.WaterLogDao
import com.ascend75.core.database.dao.WorkoutSessionDao
import com.ascend75.core.database.entities.ProgressPhotoEntity
import com.ascend75.core.database.entities.WaterLogEntity
import com.ascend75.core.datastore.AscendPreferencesDataSource
import com.ascend75.core.domain.model.DailyRecord
import com.ascend75.core.domain.model.ReadingSession
import com.ascend75.core.domain.model.ScienceCard
import com.ascend75.core.domain.model.TaskEntry
import com.ascend75.core.domain.model.UserPreferences
import com.ascend75.core.domain.model.VaultPhoto
import com.ascend75.core.domain.model.VaultPhotoRecord
import com.ascend75.core.domain.model.WaterLog
import com.ascend75.core.domain.model.WorkoutSession
import com.ascend75.core.domain.repository.DailyRecordRepository
import com.ascend75.core.domain.repository.ReadingRepository
import com.ascend75.core.domain.repository.ScienceRepository
import com.ascend75.core.domain.repository.SettingsRepository
import com.ascend75.core.domain.repository.VaultRepository
import com.ascend75.core.domain.repository.WaterRepository
import com.ascend75.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSettingsRepository @Inject constructor(
    private val preferencesDataSource: AscendPreferencesDataSource
) : SettingsRepository {

    override val preferences: Flow<UserPreferences> = preferencesDataSource.userPreferencesFlow

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        preferencesDataSource.setOnboardingCompleted(completed)

    override suspend fun setActiveChallengeId(id: String?) = preferencesDataSource.setActiveChallengeId(id)

    override suspend fun setSleepCutoff(hour: Int, minute: Int) = preferencesDataSource.setSleepCutoff(hour, minute)

    override suspend fun setSelectedMode(mode: String) = preferencesDataSource.setSelectedMode(mode)

    override suspend fun setBiometricEnabled(enabled: Boolean) = preferencesDataSource.setBiometricEnabled(enabled)

    override suspend fun setLastCelebratedDay(day: Int) = preferencesDataSource.setLastCelebratedDay(day)

    override suspend fun clearAll() = preferencesDataSource.clearAllPreferences()
}

@Singleton
class DefaultWaterRepository @Inject constructor(
    private val waterLogDao: WaterLogDao
) : WaterRepository {

    override suspend fun logsForTask(taskId: String): List<WaterLog> =
        waterLogDao.getSince(taskId, 0L).map { it.toDomain() }

    override fun observeLogsForTask(taskId: String): Flow<List<WaterLog>> =
        waterLogDao.observeForTask(taskId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun addLog(id: String, taskEntryId: String, amountMl: Int, loggedAt: Long) =
        waterLogDao.insert(
            WaterLogEntity(id = id, taskEntryId = taskEntryId, amountMl = amountMl, loggedAt = loggedAt)
        )

    override suspend fun allLogs(): List<WaterLog> = waterLogDao.getAll().map { it.toDomain() }

    override suspend fun clearAll() = waterLogDao.clearAll()
}

@Singleton
class DefaultReadingRepository @Inject constructor(
    private val dao: ReadingSessionDao
) : ReadingRepository {

    override suspend fun latestForTask(taskId: String): ReadingSession? = dao.getLatestForTask(taskId)?.toDomain()

    override fun observeSessionsForTask(taskId: String): Flow<List<ReadingSession>> =
        dao.observeForTask(taskId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun addSession(session: ReadingSession) = dao.insert(session.toEntity())

    override suspend fun allSessions(): List<ReadingSession> = dao.getAll().map { it.toDomain() }

    override suspend fun clearAll() = dao.clearAll()
}

@Singleton
class DefaultWorkoutRepository @Inject constructor(
    private val dao: WorkoutSessionDao
) : WorkoutRepository {

    override suspend fun latestForTask(taskId: String): WorkoutSession? = dao.getLatestForTask(taskId)?.toDomain()

    override fun observeSessionsForTask(taskId: String): Flow<List<WorkoutSession>> =
        dao.observeForTask(taskId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun addSession(session: WorkoutSession) = dao.insert(session.toEntity())

    override suspend fun allSessions(): List<WorkoutSession> = dao.getAll().map { it.toDomain() }

    override suspend fun clearAll() = dao.clearAll()
}

@Singleton
class DefaultVaultRepository @Inject constructor(
    private val dao: ProgressPhotoDao
) : VaultRepository {

    override fun observeVaultPhotos(): Flow<List<VaultPhoto>> =
        dao.observeVaultPhotos().map { rows -> rows.map { it.toDomain() } }

    override suspend fun photoCount(): Int = dao.getCount()

    override suspend fun addPhoto(
        id: String,
        taskEntryId: String,
        encryptedFilePath: String,
        photoHash: String,
        fileSizeBytes: Long,
        capturedAt: Long
    ) = dao.insert(
        ProgressPhotoEntity(
            id = id,
            taskEntryId = taskEntryId,
            encryptedFilePath = encryptedFilePath,
            photoHash = photoHash,
            fileSizeBytes = fileSizeBytes,
            capturedAt = capturedAt
        )
    )

    override suspend fun allPhotoRecords(): List<VaultPhotoRecord> = dao.getAll().map { it.toDomain() }

    override suspend fun clearAll() = dao.clearAll()
}

@Singleton
class DefaultScienceRepository @Inject constructor(
    private val dao: ScienceCardDao
) : ScienceRepository {

    override fun observeCardByDay(dayNumber: Int): Flow<ScienceCard?> =
        dao.observeCardByDay(dayNumber).map { it?.toDomain() }

    override fun observeUnlockedCards(unlockedUpToDay: Int): Flow<List<ScienceCard>> =
        dao.observeUnlockedCards(unlockedUpToDay).map { rows -> rows.map { it.toDomain() } }

    override fun observeUnlockedCardsByCategory(unlockedUpToDay: Int, category: String): Flow<List<ScienceCard>> =
        dao.observeUnlockedCardsByCategory(unlockedUpToDay, category).map { rows -> rows.map { it.toDomain() } }

    override fun observeBookmarkedCards(unlockedUpToDay: Int): Flow<List<ScienceCard>> =
        dao.observeBookmarkedCards(unlockedUpToDay).map { rows -> rows.map { it.toDomain() } }

    override suspend fun setBookmarked(dayNumber: Int, isBookmarked: Boolean) = dao.updateBookmark(dayNumber, isBookmarked)

    override suspend fun cardCount(): Int = dao.getCardCount()
}

@Singleton
class DefaultDailyRecordRepository @Inject constructor(
    private val dao: DailyRecordDao
) : DailyRecordRepository {

    override fun observeRecordsForChallenge(challengeId: String): Flow<List<DailyRecord>> =
        dao.observeDailyRecordsForChallenge(challengeId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun recordsForChallenge(challengeId: String): List<DailyRecord> =
        dao.getDailyRecordsForChallenge(challengeId).map { it.toDomain() }

    override suspend fun tasksForDay(challengeId: String, dayNumber: Int): List<TaskEntry> =
        dao.getDailyRecordWithTasks(challengeId, dayNumber)?.tasks?.map { it.toDomain() }.orEmpty()

    override suspend fun latestDayNumber(challengeId: String): Int? = dao.getLatestDayNumber(challengeId)

    override suspend fun clearAll() = dao.clearAllDailyRecords()
}

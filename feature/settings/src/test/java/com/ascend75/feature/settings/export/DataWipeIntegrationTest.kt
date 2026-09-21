package com.ascend75.feature.settings.export

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ascend75.core.database.AscendDatabase
import com.ascend75.core.database.entities.ChallengeInstanceEntity
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.ProgressPhotoEntity
import com.ascend75.core.database.entities.ReadingSessionEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.database.entities.WaterLogEntity
import com.ascend75.core.database.entities.WorkoutSessionEntity
import com.ascend75.core.datastore.AscendPreferencesDataSource
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

/**
 * The wipe and the export are the two places where "everything" has to actually mean everything, so
 * both are exercised against a real schema rather than a mock echo.
 */
@RunWith(RobolectricTestRunner::class)
class DataWipeIntegrationTest {

    private lateinit var database: AscendDatabase
    private lateinit var preferencesDataSource: AscendPreferencesDataSource
    private lateinit var exportManager: DataExportManager

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AscendDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        preferencesDataSource = mockk(relaxed = true)
        exportManager = DataExportManager(
            context = context,
            challengeDao = database.challengeDao(),
            dailyRecordDao = database.dailyRecordDao(),
            taskEntryDao = database.taskEntryDao(),
            workoutSessionDao = database.workoutSessionDao(),
            waterLogDao = database.waterLogDao(),
            readingSessionDao = database.readingSessionDao(),
            progressPhotoDao = database.progressPhotoDao(),
            preferencesDataSource = preferencesDataSource
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun completeWipeRemovesEveryRowAndTearsDownTheVault() = runBlocking {
        seedFullHistory()
        var vaultWiped = false

        exportManager.executeCompleteDataWipe(onWipeVault = { vaultWiped = true })

        assertTrue("The vault wipe must run as part of the erase", vaultWiped)
        assertNull(database.challengeDao().getActiveChallenge())
        assertTrue(database.dailyRecordDao().getDailyRecordsForChallenge(challengeId).isEmpty())
        assertNull(database.taskEntryDao().getTaskById(taskId))
        assertTrue(database.waterLogDao().getAll().isEmpty())
        assertTrue(database.readingSessionDao().getAll().isEmpty())
        assertTrue(database.workoutSessionDao().getAll().isEmpty())
        assertEquals(0, database.progressPhotoDao().getCount())
        coVerify { preferencesDataSource.clearAllPreferences() }
    }

    @Test
    fun exportArchiveCarriesTheWholeHistoryNotJustTheChallengeHeader() = runBlocking {
        seedFullHistory()

        val archive = exportManager.exportToFile().readText()

        val root = JSONObject(archive)
        assertTrue("The archive must include the active challenge", root.has("challenges"))
        assertEquals(1, root.getJSONArray("challenges").length())

        val records = root.getJSONArray("dailyRecords")
        assertEquals(1, records.length())
        val tasks = records.getJSONObject(0).getJSONArray("tasks")
        assertEquals("Daily records must carry their task rows", 1, tasks.length())

        assertEquals(1, root.getJSONArray("waterLogs").length())
        assertEquals(1, root.getJSONArray("workoutSessions").length())
        assertEquals(1, root.getJSONArray("readingSessions").length())
        assertEquals(1, root.getJSONArray("progressPhotos").length())
    }

    private val challengeId = UUID.randomUUID().toString()
    private val recordId = UUID.randomUUID().toString()
    private val taskId = UUID.randomUUID().toString()

    private suspend fun seedFullHistory() {
        val now = System.currentTimeMillis()
        database.challengeDao().insertChallenge(
            ChallengeInstanceEntity(
                id = challengeId,
                attemptNumber = 1,
                mode = "STRICT_75",
                status = "ACTIVE",
                startedAt = now,
                configJson = "{}"
            )
        )
        database.dailyRecordDao().insertDailyRecord(
            DailyRecordEntity(
                id = recordId,
                challengeInstanceId = challengeId,
                dayNumber = 1,
                calendarDate = 19_000L,
                sleepCutoffTimestamp = now + 3_600_000L
            )
        )
        database.taskEntryDao().insertTaskEntry(
            TaskEntryEntity(
                id = taskId,
                dailyRecordId = recordId,
                habitType = "WORKOUT_1",
                targetValue = 45.0,
                currentValue = 45.0
            )
        )
        database.workoutSessionDao().insert(
            WorkoutSessionEntity(
                id = UUID.randomUUID().toString(),
                taskEntryId = taskId,
                durationSeconds = 2700,
                isOutdoor = true,
                workoutType = "RUNNING",
                intensity = "MODERATE",
                startedAt = now,
                finishedAt = now + 2_700_000L
            )
        )
        database.waterLogDao().insert(
            WaterLogEntity(id = UUID.randomUUID().toString(), taskEntryId = taskId, amountMl = 500, loggedAt = now)
        )
        database.readingSessionDao().insert(
            ReadingSessionEntity(
                id = UUID.randomUUID().toString(),
                taskEntryId = taskId,
                bookTitle = "Deep Work",
                startPage = 1,
                endPage = 11,
                pagesRead = 10
            )
        )
        database.progressPhotoDao().insert(
            ProgressPhotoEntity(
                id = UUID.randomUUID().toString(),
                taskEntryId = taskId,
                encryptedFilePath = "/vault/day-1.enc",
                photoHash = "abc",
                fileSizeBytes = 1234L
            )
        )
    }
}

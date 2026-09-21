package com.ascend75.feature.settings.export

import android.content.Context
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.ProgressPhotoDao
import com.ascend75.core.database.dao.ReadingSessionDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.dao.WaterLogDao
import com.ascend75.core.database.dao.WorkoutSessionDao
import com.ascend75.core.datastore.AscendPreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val challengeDao: ChallengeDao,
    private val dailyRecordDao: DailyRecordDao,
    private val taskEntryDao: TaskEntryDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val waterLogDao: WaterLogDao,
    private val readingSessionDao: ReadingSessionDao,
    private val progressPhotoDao: ProgressPhotoDao,
    private val preferencesDataSource: AscendPreferencesDataSource
) {

    /**
     * Serialises the whole challenge history — every daily record with its tasks, plus every
     * workout, hydration, reading and vault row — into one JSON document in the cache dir.
     */
    suspend fun exportToFile(): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("schemaVersion", 1)

        val challengesArray = JSONArray()
        val active = challengeDao.getActiveChallenge()
        if (active != null) {
            challengesArray.put(
                JSONObject().apply {
                    put("id", active.id)
                    put("mode", active.mode)
                    put("attemptNumber", active.attemptNumber)
                    put("status", active.status)
                    put("startedAt", active.startedAt)
                    put("endedAt", active.endedAt)
                    put("configJson", active.configJson)
                }
            )

            val dailyRecordsArray = JSONArray()
            val records = dailyRecordDao.getDailyRecordsForChallenge(active.id)
            for (record in records) {
                val tasksArray = JSONArray()
                val withTasks = dailyRecordDao.getDailyRecordWithTasks(active.id, record.dayNumber)
                withTasks?.tasks?.forEach { task ->
                    tasksArray.put(
                        JSONObject().apply {
                            put("id", task.id)
                            put("habitType", task.habitType)
                            put("isCompleted", task.isCompleted)
                            put("completedAt", task.completedAt)
                            put("targetValue", task.targetValue)
                            put("currentValue", task.currentValue)
                            put("notes", task.notes)
                        }
                    )
                }
                dailyRecordsArray.put(
                    JSONObject().apply {
                        put("id", record.id)
                        put("dayNumber", record.dayNumber)
                        put("calendarDate", record.calendarDate)
                        put("isCompleted", record.isCompleted)
                        put("sleepCutoffTimestamp", record.sleepCutoffTimestamp)
                        put("reflectionNotes", record.reflectionNotes)
                        put("tasks", tasksArray)
                    }
                )
            }
            root.put("dailyRecords", dailyRecordsArray)
        }
        root.put("challenges", challengesArray)

        root.put(
            "workoutSessions",
            JSONArray().apply {
                workoutSessionDao.getAll().forEach { session ->
                    put(
                        JSONObject().apply {
                            put("id", session.id)
                            put("taskEntryId", session.taskEntryId)
                            put("durationSeconds", session.durationSeconds)
                            put("isOutdoor", session.isOutdoor)
                            put("workoutType", session.workoutType)
                            put("intensity", session.intensity)
                            put("startedAt", session.startedAt)
                            put("finishedAt", session.finishedAt)
                            put("notes", session.notes)
                        }
                    )
                }
            }
        )

        root.put(
            "waterLogs",
            JSONArray().apply {
                waterLogDao.getAll().forEach { log ->
                    put(
                        JSONObject().apply {
                            put("id", log.id)
                            put("taskEntryId", log.taskEntryId)
                            put("amountMl", log.amountMl)
                            put("loggedAt", log.loggedAt)
                        }
                    )
                }
            }
        )

        root.put(
            "readingSessions",
            JSONArray().apply {
                readingSessionDao.getAll().forEach { session ->
                    put(
                        JSONObject().apply {
                            put("id", session.id)
                            put("taskEntryId", session.taskEntryId)
                            put("bookTitle", session.bookTitle)
                            put("bookAuthor", session.bookAuthor)
                            put("startPage", session.startPage)
                            put("endPage", session.endPage)
                            put("pagesRead", session.pagesRead)
                            put("readingDurationSeconds", session.readingDurationSeconds)
                            put("keyTakeaway", session.keyTakeaway)
                            put("loggedAt", session.loggedAt)
                        }
                    )
                }
            }
        )

        root.put(
            "progressPhotos",
            JSONArray().apply {
                progressPhotoDao.getAll().forEach { photo ->
                    put(
                        JSONObject().apply {
                            put("id", photo.id)
                            put("taskEntryId", photo.taskEntryId)
                            put("encryptedFilePath", photo.encryptedFilePath)
                            put("photoHash", photo.photoHash)
                            put("fileSizeBytes", photo.fileSizeBytes)
                            put("capturedAt", photo.capturedAt)
                        }
                    )
                }
            }
        )

        val exportDir = File(context.cacheDir, "export").apply { mkdirs() }
        val target = File(exportDir, "ascend75-export-${System.currentTimeMillis()}.json")
        target.writeText(root.toString(2))
        target
    }

    suspend fun executeCompleteDataWipe(onWipeVault: suspend () -> Unit) = withContext(Dispatchers.IO) {
        // 1. Wipe photo vault with zero-fill
        onWipeVault()

        // 2. Truncate Room database tables
        progressPhotoDao.clearAll()
        readingSessionDao.clearAll()
        waterLogDao.clearAll()
        workoutSessionDao.clearAll()
        taskEntryDao.clearAllTasks()
        dailyRecordDao.clearAllDailyRecords()
        challengeDao.clearAllChallenges()

        // 3. Clear DataStore preferences
        preferencesDataSource.clearAllPreferences()
    }
}

package com.ascend75.core.common.export

import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.datastore.AscendPreferencesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val dailyRecordDao: DailyRecordDao,
    private val taskEntryDao: TaskEntryDao,
    private val preferencesDataSource: AscendPreferencesDataSource
) {
    suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("appVersion", "1.0.0")

        val challengesArray = JSONArray()
        val active = challengeDao.getActiveChallenge()
        if (active != null) {
            val challengeObj = JSONObject().apply {
                put("id", active.id)
                put("mode", active.mode)
                put("attemptNumber", active.attemptNumber)
                put("status", active.status)
                put("startedAt", active.startedAt)
                put("endedAt", active.endedAt)
            }
            challengesArray.put(challengeObj)
        }
        root.put("challenges", challengesArray)
        root.toString(2)
    }

    suspend fun executeCompleteDataWipe(onWipeVault: () -> Unit) = withContext(Dispatchers.IO) {
        // 1. Wipe photo vault with zero-fill
        onWipeVault()

        // 2. Truncate Room database tables
        taskEntryDao.clearAllTasks()
        dailyRecordDao.clearAllDailyRecords()
        challengeDao.clearAllChallenges()

        // 3. Clear DataStore preferences
        preferencesDataSource.clearAllPreferences()
    }
}

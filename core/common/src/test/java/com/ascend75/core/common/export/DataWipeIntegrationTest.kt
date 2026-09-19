package com.ascend75.core.common.export

import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.datastore.AscendPreferencesDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DataWipeIntegrationTest {

    @Test
    fun verifyCompleteDataWipeInvokesAllPurgeRoutines() = runTest {
        val challengeDao = mockk<ChallengeDao>(relaxed = true)
        val dailyRecordDao = mockk<DailyRecordDao>(relaxed = true)
        val taskEntryDao = mockk<TaskEntryDao>(relaxed = true)
        val preferencesDataSource = mockk<AscendPreferencesDataSource>(relaxed = true)

        var vaultWiped = false

        val exportManager = DataExportManager(
            challengeDao = challengeDao,
            dailyRecordDao = dailyRecordDao,
            taskEntryDao = taskEntryDao,
            preferencesDataSource = preferencesDataSource
        )

        exportManager.executeCompleteDataWipe(
            onWipeVault = { vaultWiped = true }
        )

        assert(vaultWiped)
        coVerify { taskEntryDao.clearAllTasks() }
        coVerify { dailyRecordDao.clearAllDailyRecords() }
        coVerify { challengeDao.clearAllChallenges() }
        coVerify { preferencesDataSource.clearAllPreferences() }
    }
}

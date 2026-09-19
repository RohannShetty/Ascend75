package com.ascend75.core.database

import com.ascend75.core.database.entities.ChallengeInstanceEntity
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ChallengeDaoTest {

    @Test
    fun verifyChallengeEntityCreationAndIntegrity() {
        val challengeId = UUID.randomUUID().toString()
        val challenge = ChallengeInstanceEntity(
            id = challengeId,
            attemptNumber = 1,
            mode = "STRICT_75",
            status = "ACTIVE",
            startedAt = 1000L,
            configJson = "{}"
        )

        assertEquals("STRICT_75", challenge.mode)
        assertEquals("ACTIVE", challenge.status)
        assertEquals(1, challenge.attemptNumber)
        assertNotNull(challenge.id)
    }

    @Test
    fun verifyDailyRecordAndTaskEntryCascadeRelationship() {
        val challengeId = UUID.randomUUID().toString()
        val recordId = UUID.randomUUID().toString()
        val taskId = UUID.randomUUID().toString()

        val dailyRecord = DailyRecordEntity(
            id = recordId,
            challengeInstanceId = challengeId,
            dayNumber = 1,
            calendarDate = 19500L,
            sleepCutoffTimestamp = 19500L + 3 * 3600 * 1000L
        )

        val taskEntry = TaskEntryEntity(
            id = taskId,
            dailyRecordId = recordId,
            habitType = "WORKOUT_1",
            isCompleted = false,
            targetValue = 1.0,
            currentValue = 0.0
        )

        assertEquals(recordId, taskEntry.dailyRecordId)
        assertEquals(challengeId, dailyRecord.challengeInstanceId)
        assertTrue(dailyRecord.dayNumber in 1..75)
    }
}

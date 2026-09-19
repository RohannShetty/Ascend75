package com.ascend75.core.common.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ChallengeRulesEngineTest {

    @Test
    fun verifyStrict75RequiresAllTasksAndTriggersResetWhenIncompletePastCutoff() {
        val tasks = ChallengeRulesEngine.getTasksForMode(ChallengeMode.STRICT_75)
        assertEquals(6, tasks.size)

        // 5 of 6 completed, past sleep cutoff -> StrictResetRequired
        val transition = ChallengeRulesEngine.evaluateDayTransition(
            currentDayNumber = 14,
            totalTasks = 6,
            completedTasks = 5,
            isPastCutoff = true,
            mode = ChallengeMode.STRICT_75
        )

        assertTrue(transition is DayTransitionResult.StrictResetRequired)
        val reset = transition as DayTransitionResult.StrictResetRequired
        assertEquals(14, reset.failedDayNumber)
        assertEquals(5, reset.completedCount)
    }

    @Test
    fun verifyFlexible75AdvancesWithoutResetWhenIncomplete() {
        val transition = ChallengeRulesEngine.evaluateDayTransition(
            currentDayNumber = 14,
            totalTasks = 6,
            completedTasks = 4,
            isPastCutoff = true,
            mode = ChallengeMode.FLEXIBLE_75
        )

        assertTrue(transition is DayTransitionResult.IncompleteAdvance)
        val advance = transition as DayTransitionResult.IncompleteAdvance
        assertEquals(15, advance.nextDayNumber)
        assertEquals(4, advance.completedCount)
    }

    @Test
    fun verifyCompletedDayAdvancesToNextDayPastCutoff() {
        val transition = ChallengeRulesEngine.evaluateDayTransition(
            currentDayNumber = 20,
            totalTasks = 6,
            completedTasks = 6,
            isPastCutoff = true,
            mode = ChallengeMode.STRICT_75
        )

        assertTrue(transition is DayTransitionResult.CompletedAdvance)
        assertEquals(21, (transition as DayTransitionResult.CompletedAdvance).nextDayNumber)
    }

    @Test
    fun verifySleepCutoffCalculationAcrossMidnight() {
        val today = LocalDate.of(2026, 10, 1)
        val cutoffTime = LocalTime.of(3, 0)
        val zoneId = ZoneId.of("UTC")

        val cutoffTimestamp = DayBoundaryEvaluator.calculateSleepCutoffTimestamp(
            calendarDate = today,
            sleepCutoffTime = cutoffTime,
            zoneId = zoneId
        )

        // 2:00 AM UTC on Oct 2 is before cutoff
        val earlyMorningBeforeCutoff = today.plusDays(1).atTime(2, 0).atZone(zoneId).toInstant().toEpochMilli()
        val isPastCutoffEarly = DayBoundaryEvaluator.isPastSleepCutoff(earlyMorningBeforeCutoff, cutoffTimestamp)
        assertEquals(false, isPastCutoffEarly)

        // 3:30 AM UTC on Oct 2 is past cutoff
        val morningAfterCutoff = today.plusDays(1).atTime(3, 30).atZone(zoneId).toInstant().toEpochMilli()
        val isPastCutoffLate = DayBoundaryEvaluator.isPastSleepCutoff(morningAfterCutoff, cutoffTimestamp)
        assertEquals(true, isPastCutoffLate)
    }
}

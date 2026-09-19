package com.ascend75.app

import com.ascend75.core.common.domain.ChallengeMode
import com.ascend75.core.common.domain.ChallengeRulesEngine
import com.ascend75.core.common.domain.DayBoundaryEvaluator
import com.ascend75.core.common.domain.DayTransitionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AscendEndToEndJourneyTest {

    @Test
    fun executeFullUserJourneySimulation() {
        // 1. User starts onboarding and selects Strict 75
        val mode = ChallengeMode.STRICT_75
        val tasks = ChallengeRulesEngine.getTasksForMode(mode)
        assertEquals("Strict 75 must require 6 core habits", 6, tasks.size)

        // 2. Schedule evaluation for Day 1
        val day1 = LocalDate.of(2026, 10, 1)
        val cutoffTime = LocalTime.of(3, 0)
        val cutoffTimestamp = DayBoundaryEvaluator.calculateSleepCutoffTimestamp(
            calendarDate = day1,
            sleepCutoffTime = cutoffTime,
            zoneId = ZoneId.of("UTC")
        )

        // 3. User logs workouts and water before midnight
        val completedTasksDay1 = 6
        val isPastCutoffDay1 = false
        val day1Result = ChallengeRulesEngine.evaluateDayTransition(
            currentDayNumber = 1,
            totalTasks = 6,
            completedTasks = completedTasksDay1,
            isPastCutoff = isPastCutoffDay1,
            mode = mode
        )
        assertEquals(DayTransitionResult.Ongoing, day1Result)

        // 4. Day 2: User completes 5 of 6 tasks and falls asleep past 3:00 AM cutoff
        val completedTasksDay2 = 5
        val isPastCutoffDay2 = true
        val day2Result = ChallengeRulesEngine.evaluateDayTransition(
            currentDayNumber = 2,
            totalTasks = 6,
            completedTasks = completedTasksDay2,
            isPastCutoff = isPastCutoffDay2,
            mode = mode
        )
        assertTrue(
            "Strict 75 must require reset when incomplete past sleep cutoff",
            day2Result is DayTransitionResult.StrictResetRequired
        )

        val reset = day2Result as DayTransitionResult.StrictResetRequired
        assertEquals(2, reset.failedDayNumber)
        assertEquals(5, reset.completedCount)

        // 5. User switches to Flexible 75 on restart; incomplete day advances safely
        val flexibleResult = ChallengeRulesEngine.evaluateDayTransition(
            currentDayNumber = 2,
            totalTasks = 6,
            completedTasks = 5,
            isPastCutoff = true,
            mode = ChallengeMode.FLEXIBLE_75
        )
        assertTrue(
            "Flexible 75 advances without resetting to Day 1",
            flexibleResult is DayTransitionResult.IncompleteAdvance
        )
        assertEquals(3, (flexibleResult as DayTransitionResult.IncompleteAdvance).nextDayNumber)
    }
}

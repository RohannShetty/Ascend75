package com.ascend75.core.common.domain

import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.domain.model.HabitType

data class TaskSpec(
    val habitType: HabitType,
    val title: String,
    val category: String,
    val targetValue: Double,
    val unit: String
)

sealed interface DayTransitionResult {
    data object Ongoing : DayTransitionResult
    data class CompletedAdvance(val nextDayNumber: Int) : DayTransitionResult
    data class IncompleteAdvance(val nextDayNumber: Int, val completedCount: Int, val totalCount: Int) : DayTransitionResult
    data class StrictResetRequired(val failedDayNumber: Int, val completedCount: Int, val totalCount: Int) : DayTransitionResult
    data object ChallengeFinished : DayTransitionResult
}

object ChallengeRulesEngine {

    val CoreHabits = listOf(
        TaskSpec(
            habitType = HabitType.WORKOUT_1,
            title = "Outdoor Workout (45m)",
            category = "Physical Discipline",
            targetValue = 45.0,
            unit = "minutes"
        ),
        TaskSpec(
            habitType = HabitType.WORKOUT_2,
            title = "Second Workout (45m)",
            category = "Physical Discipline",
            targetValue = 45.0,
            unit = "minutes"
        ),
        TaskSpec(
            habitType = HabitType.WATER,
            title = "Hydration Intake (3.8L)",
            category = "Physiological Fuel",
            targetValue = 3800.0,
            unit = "ml"
        ),
        TaskSpec(
            habitType = HabitType.READING,
            title = "Read 10 Pages (Non-Fiction)",
            category = "Cognitive Growth",
            targetValue = 10.0,
            unit = "pages"
        ),
        TaskSpec(
            habitType = HabitType.DIET,
            title = "Strict Diet Adherence",
            category = "Nutrition Integrity",
            targetValue = 1.0,
            unit = "check"
        ),
        TaskSpec(
            habitType = HabitType.PHOTO,
            title = "Progress Photo (Vault)",
            category = "Visual Accountability",
            targetValue = 1.0,
            unit = "photo"
        )
    )

    fun getTasksForMode(mode: ChallengeMode): List<TaskSpec> {
        return when (mode) {
            ChallengeMode.STRICT_75, ChallengeMode.FLEXIBLE_75 -> CoreHabits
            ChallengeMode.SOFT_75 -> listOf(
                TaskSpec(HabitType.WORKOUT_1, "Daily Workout (45m)", "Physical Discipline", 45.0, "minutes"),
                TaskSpec(HabitType.WATER, "Hydration Intake (3.0L)", "Physiological Fuel", 3000.0, "ml"),
                TaskSpec(HabitType.READING, "Read 10 Pages", "Cognitive Growth", 10.0, "pages"),
                TaskSpec(HabitType.DIET, "Mindful Clean Diet", "Nutrition Integrity", 1.0, "check")
            )
            ChallengeMode.CUSTOM -> CoreHabits
        }
    }

    /**
     * Evaluates day boundary transition when current timestamp is evaluated against sleep cutoff.
     */
    fun evaluateDayTransition(
        currentDayNumber: Int,
        totalTasks: Int,
        completedTasks: Int,
        isPastCutoff: Boolean,
        mode: ChallengeMode
    ): DayTransitionResult {
        val allCompleted = completedTasks >= totalTasks

        if (allCompleted) {
            return if (currentDayNumber >= 75) {
                DayTransitionResult.ChallengeFinished
            } else if (isPastCutoff) {
                DayTransitionResult.CompletedAdvance(currentDayNumber + 1)
            } else {
                DayTransitionResult.Ongoing
            }
        }

        if (!isPastCutoff) {
            return DayTransitionResult.Ongoing
        }

        // Past sleep cutoff with incomplete tasks:
        return when (mode) {
            ChallengeMode.STRICT_75 -> {
                DayTransitionResult.StrictResetRequired(
                    failedDayNumber = currentDayNumber,
                    completedCount = completedTasks,
                    totalCount = totalTasks
                )
            }
            ChallengeMode.FLEXIBLE_75, ChallengeMode.SOFT_75, ChallengeMode.CUSTOM -> {
                if (currentDayNumber >= 75) {
                    DayTransitionResult.ChallengeFinished
                } else {
                    DayTransitionResult.IncompleteAdvance(
                        nextDayNumber = currentDayNumber + 1,
                        completedCount = completedTasks,
                        totalCount = totalTasks
                    )
                }
            }
        }
    }
}

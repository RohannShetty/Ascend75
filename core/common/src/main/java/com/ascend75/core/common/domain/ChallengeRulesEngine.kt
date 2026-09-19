package com.ascend75.core.common.domain

data class TaskSpec(
    val habitType: String,
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
            habitType = "WORKOUT_1",
            title = "Outdoor Workout (45m)",
            category = "Physical Discipline",
            targetValue = 45.0,
            unit = "minutes"
        ),
        TaskSpec(
            habitType = "WORKOUT_2",
            title = "Second Workout (45m)",
            category = "Physical Discipline",
            targetValue = 45.0,
            unit = "minutes"
        ),
        TaskSpec(
            habitType = "WATER",
            title = "Hydration Intake (3.8L)",
            category = "Physiological Fuel",
            targetValue = 3800.0,
            unit = "ml"
        ),
        TaskSpec(
            habitType = "READING",
            title = "Read 10 Pages (Non-Fiction)",
            category = "Cognitive Growth",
            targetValue = 10.0,
            unit = "pages"
        ),
        TaskSpec(
            habitType = "DIET",
            title = "Strict Diet Adherence",
            category = "Nutrition Integrity",
            targetValue = 1.0,
            unit = "check"
        ),
        TaskSpec(
            habitType = "PHOTO",
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
                TaskSpec("WORKOUT_1", "Daily Workout (45m)", "Physical Discipline", 45.0, "minutes"),
                TaskSpec("WATER", "Hydration Intake (3.0L)", "Physiological Fuel", 3000.0, "ml"),
                TaskSpec("READING", "Read 10 Pages", "Cognitive Growth", 10.0, "pages"),
                TaskSpec("DIET", "Mindful Clean Diet", "Nutrition Integrity", 1.0, "check")
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

package com.ascend75.core.domain.model

enum class ChallengeMode(val title: String, val description: String) {
    STRICT_75(
        title = "Strict 75",
        description = "Uncompromising discipline. Two 45m workouts (one outdoor), 3.8L water, 10 pages reading, clean diet, progress photo. Missing any task past sleep cutoff requires restarting at Day 1."
    ),
    FLEXIBLE_75(
        title = "Flexible 75",
        description = "Adaptive high performance. Identical rigor to Strict 75, but incomplete days log reflection notes and advance without punitive Day 1 resets."
    ),
    SOFT_75(
        title = "75 Soft",
        description = "Sustainable consistency. One 45m workout daily, active recovery, 3L water, 10 pages reading, balanced mindful nutrition."
    ),
    CUSTOM(
        title = "Custom",
        description = "Tailored challenge with customized habit targets and parameters."
    );

    companion object {
        fun fromString(value: String): ChallengeMode {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: STRICT_75
        }
    }
}

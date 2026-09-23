package com.ascend75.core.domain.model

/**
 * The canonical set of tracked habits. Database rows store [raw]; nothing above the data layer
 * should ever compare raw strings again.
 */
enum class HabitType(val raw: String) {
    WORKOUT_1("WORKOUT_1"),
    WORKOUT_2("WORKOUT_2"),
    WATER("WATER"),
    READING("READING"),
    DIET("DIET"),
    PHOTO("PHOTO"),
    CUSTOM("CUSTOM");

    val isWorkout: Boolean get() = this == WORKOUT_1 || this == WORKOUT_2

    companion object {
        /** Unknown or legacy values degrade to [CUSTOM] instead of throwing. */
        fun fromRaw(value: String): HabitType =
            entries.firstOrNull { it.raw.equals(value, ignoreCase = true) } ?: CUSTOM
    }
}

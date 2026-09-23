package com.ascend75.core.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HabitTypeTest {

    @Test
    fun everyPersistedRawValueResolvesToItsEnum() {
        HabitType.entries.forEach { type ->
            assertThat(HabitType.fromRaw(type.raw)).isEqualTo(type)
        }
    }

    @Test
    fun unknownValuesDegradeToCustomInsteadOfThrowing() {
        assertThat(HabitType.fromRaw("SOMETHING_ELSE")).isEqualTo(HabitType.CUSTOM)
    }

    @Test
    fun onlyTheTwoWorkoutHabitsAreWorkouts() {
        assertThat(HabitType.entries.filter { it.isWorkout })
            .containsExactly(HabitType.WORKOUT_1, HabitType.WORKOUT_2)
    }
}

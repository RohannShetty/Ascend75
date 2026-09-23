package com.ascend75.core.data.mapper

import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.domain.model.HabitType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MappersTest {

    @Test
    fun taskEntryRoundTripIsLossless() {
        val entity = TaskEntryEntity(
            id = "t1",
            dailyRecordId = "d1",
            habitType = "WORKOUT_2",
            isCompleted = true,
            completedAt = 123L,
            notes = "note",
            targetValue = 45.0,
            currentValue = 30.0
        )

        assertThat(entity.toDomain().toEntity()).isEqualTo(entity)
    }

    @Test
    fun unknownHabitTypeRawValueDegradesToCustomInsteadOfThrowing() {
        val entity = TaskEntryEntity(id = "t2", dailyRecordId = "d1", habitType = "LEGACY_THING")

        assertThat(entity.toDomain().habitType).isEqualTo(HabitType.CUSTOM)
    }
}

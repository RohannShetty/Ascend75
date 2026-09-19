package com.ascend75.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.ScienceCardDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.entities.ChallengeInstanceEntity
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.ProgressPhotoEntity
import com.ascend75.core.database.entities.ReadingSessionEntity
import com.ascend75.core.database.entities.ScienceCardEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.database.entities.WaterLogEntity
import com.ascend75.core.database.entities.WorkoutSessionEntity

@Database(
    entities = [
        ChallengeInstanceEntity::class,
        DailyRecordEntity::class,
        TaskEntryEntity::class,
        WorkoutSessionEntity::class,
        WaterLogEntity::class,
        ReadingSessionEntity::class,
        ProgressPhotoEntity::class,
        ScienceCardEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AscendDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun taskEntryDao(): TaskEntryDao
    abstract fun scienceCardDao(): ScienceCardDao
}

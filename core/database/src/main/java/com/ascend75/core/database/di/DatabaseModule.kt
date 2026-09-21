package com.ascend75.core.database.di

import android.content.Context
import androidx.room.Room
import com.ascend75.core.database.AscendDatabase
import com.ascend75.core.database.ScienceCardSeeder
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.ProgressPhotoDao
import com.ascend75.core.database.dao.ReadingSessionDao
import com.ascend75.core.database.dao.ScienceCardDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.dao.WaterLogDao
import com.ascend75.core.database.dao.WorkoutSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAscendDatabase(
        @ApplicationContext context: Context,
        scienceCardDaoProvider: Provider<ScienceCardDao>
    ): AscendDatabase {
        return Room.databaseBuilder(
            context,
            AscendDatabase::class.java,
            "ascend75.db"
        )
            .addCallback(ScienceCardSeeder(context, scienceCardDaoProvider))
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChallengeDao(db: AscendDatabase): ChallengeDao = db.challengeDao()

    @Provides
    fun provideDailyRecordDao(db: AscendDatabase): DailyRecordDao = db.dailyRecordDao()

    @Provides
    fun provideTaskEntryDao(db: AscendDatabase): TaskEntryDao = db.taskEntryDao()

    @Provides
    fun provideScienceCardDao(db: AscendDatabase): ScienceCardDao = db.scienceCardDao()

    @Provides
    fun provideWorkoutSessionDao(db: AscendDatabase): WorkoutSessionDao = db.workoutSessionDao()

    @Provides
    fun provideWaterLogDao(db: AscendDatabase): WaterLogDao = db.waterLogDao()

    @Provides
    fun provideReadingSessionDao(db: AscendDatabase): ReadingSessionDao = db.readingSessionDao()

    @Provides
    fun provideProgressPhotoDao(db: AscendDatabase): ProgressPhotoDao = db.progressPhotoDao()
}

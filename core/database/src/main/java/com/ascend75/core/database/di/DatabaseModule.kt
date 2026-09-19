package com.ascend75.core.database.di

import android.content.Context
import androidx.room.Room
import com.ascend75.core.database.AscendDatabase
import com.ascend75.core.database.ScienceCardSeeder
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.ScienceCardDao
import com.ascend75.core.database.dao.TaskEntryDao
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
}

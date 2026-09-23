package com.ascend75.core.data.di

import com.ascend75.core.data.repository.DefaultChallengeRepository
import com.ascend75.core.data.repository.DefaultDailyRecordRepository
import com.ascend75.core.data.repository.DefaultReadingRepository
import com.ascend75.core.data.repository.DefaultScienceRepository
import com.ascend75.core.data.repository.DefaultSettingsRepository
import com.ascend75.core.data.repository.DefaultTaskRepository
import com.ascend75.core.data.repository.DefaultTodayProtocolRepository
import com.ascend75.core.data.repository.DefaultVaultRepository
import com.ascend75.core.data.repository.DefaultWaterRepository
import com.ascend75.core.data.repository.DefaultWorkoutRepository
import com.ascend75.core.domain.repository.ChallengeRepository
import com.ascend75.core.domain.repository.DailyRecordRepository
import com.ascend75.core.domain.repository.ReadingRepository
import com.ascend75.core.domain.repository.ScienceRepository
import com.ascend75.core.domain.repository.SettingsRepository
import com.ascend75.core.domain.repository.TaskRepository
import com.ascend75.core.domain.repository.TodayProtocolRepository
import com.ascend75.core.domain.repository.VaultRepository
import com.ascend75.core.domain.repository.WaterRepository
import com.ascend75.core.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DefaultSettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(impl: DefaultChallengeRepository): ChallengeRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: DefaultTaskRepository): TaskRepository

    @Binds
    @Singleton
    abstract fun bindTodayProtocolRepository(impl: DefaultTodayProtocolRepository): TodayProtocolRepository

    @Binds
    @Singleton
    abstract fun bindWaterRepository(impl: DefaultWaterRepository): WaterRepository

    @Binds
    @Singleton
    abstract fun bindReadingRepository(impl: DefaultReadingRepository): ReadingRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: DefaultWorkoutRepository): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindVaultRepository(impl: DefaultVaultRepository): VaultRepository

    @Binds
    @Singleton
    abstract fun bindScienceRepository(impl: DefaultScienceRepository): ScienceRepository

    @Binds
    @Singleton
    abstract fun bindDailyRecordRepository(impl: DefaultDailyRecordRepository): DailyRecordRepository
}

package com.ascend75.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ascend75.core.notifications.NotificationChannelsManager
import com.ascend75.core.notifications.ScheduleEvaluatorWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AscendApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationChannelsManager: NotificationChannelsManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationChannelsManager.createAllChannels()

        // Nothing else enqueues the evaluator on a cold boot, so without this periodic request the
        // habit reminders would never be scheduled at all.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SCHEDULE_EVALUATOR_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ScheduleEvaluatorWorker>(6, TimeUnit.HOURS).build()
        )
    }

    private companion object {
        const val SCHEDULE_EVALUATOR_WORK = "ascend75_schedule_evaluator"
    }
}

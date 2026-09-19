package com.ascend75.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime

@HiltWorker
class ScheduleEvaluatorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val challengeDao: ChallengeDao,
    private val dailyRecordDao: DailyRecordDao,
    private val scheduler: SmartReminderScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val challenge = challengeDao.getActiveChallenge() ?: return Result.success()
        val latestDay = dailyRecordDao.getLatestDayNumber(challenge.id) ?: 1
        val dailyRecord = dailyRecordDao.getDailyRecordWithTasks(challenge.id, latestDay) ?: return Result.success()

        val pendingTasks = dailyRecord.tasks.filter { !it.isCompleted }

        // If all tasks complete, cancel any scheduled reminders
        if (pendingTasks.isEmpty()) {
            scheduler.cancelAllPendingReminders(dailyRecord.tasks.map { it.id })
            return Result.success()
        }

        // Evaluate quiet hours
        val now = LocalTime.now()
        if (!scheduler.isWithinQuietHours(now)) {
            // Schedule next nudge 2 hours from now if pending tasks remain
            val triggerTime = System.currentTimeMillis() + 2 * 60 * 60 * 1000L
            pendingTasks.firstOrNull()?.let { task ->
                scheduler.scheduleHabitNudge(triggerTime, task.id, task.habitType)
            }
        }

        return Result.success()
    }
}

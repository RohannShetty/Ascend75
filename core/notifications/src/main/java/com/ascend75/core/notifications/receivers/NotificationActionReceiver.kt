package com.ascend75.core.notifications.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.notifications.SmartReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskEntryDao: TaskEntryDao

    @Inject
    lateinit var scheduler: SmartReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId.hashCode())

        when (intent.action) {
            NotificationAlarmReceiver.ACTION_COMPLETE_TASK_DIRECT -> {
                CoroutineScope(Dispatchers.IO).launch {
                    taskEntryDao.updateTaskCompletion(taskId, isCompleted = true, completedAt = System.currentTimeMillis())
                    scheduler.cancelHabitReminder(taskId)
                }
            }
            NotificationAlarmReceiver.ACTION_SNOOZE_30_MIN -> {
                val snoozeTimestamp = System.currentTimeMillis() + 30 * 60 * 1000L
                scheduler.scheduleHabitNudge(snoozeTimestamp, taskId, "Snoozed Habit")
            }
        }
    }
}

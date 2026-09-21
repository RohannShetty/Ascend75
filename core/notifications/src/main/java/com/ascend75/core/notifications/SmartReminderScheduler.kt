package com.ascend75.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ascend75.core.notifications.receivers.NotificationAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun isWithinQuietHours(
        currentTime: LocalTime,
        quietStartHour: Int = 22,
        quietEndHour: Int = 7
    ): Boolean = isQuietHour(currentTime.hour, quietStartHour, quietEndHour)

    fun scheduleHabitNudge(
        triggerTimestamp: Long,
        taskId: String,
        habitTitle: String
    ) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_NUDGE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_HABIT_TITLE, habitTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimestamp,
            pendingIntent
        )
    }

    /**
     * Cancels any pending reminder alarms for a given habit when it is completed.
     */
    fun cancelHabitReminder(taskId: String) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_NUDGE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Cancels all scheduled evening reminder notifications when all habits are complete.
     */
    fun cancelAllPendingReminders(taskIds: List<String>) {
        taskIds.forEach { cancelHabitReminder(it) }
    }

    companion object {
        const val ACTION_TRIGGER_NUDGE = "com.ascend75.action.TRIGGER_NUDGE"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title"

        /**
         * Pure quiet-hours predicate, separated from the scheduler so it can be exercised without an
         * Android [Context] (the scheduler's initialiser resolves an AlarmManager).
         */
        @JvmStatic
        fun isQuietHour(currentHour: Int, quietStartHour: Int = 22, quietEndHour: Int = 7): Boolean =
            if (quietStartHour > quietEndHour) {
                // Window wraps midnight, e.g. 22:00 to 07:00
                currentHour >= quietStartHour || currentHour < quietEndHour
            } else {
                currentHour in quietStartHour until quietEndHour
            }
    }
}

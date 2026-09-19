package com.ascend75.core.notifications.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.ascend75.core.notifications.NotificationChannelsManager
import com.ascend75.core.notifications.SmartReminderScheduler

class NotificationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(SmartReminderScheduler.EXTRA_TASK_ID) ?: return
        val habitTitle = intent.getStringExtra(SmartReminderScheduler.EXTRA_HABIT_TITLE) ?: "Pending Habit"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Deep link intent to Dashboard with task expanded
        val deepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("ascend75://task/$taskId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action button 1: Complete Task directly
        val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK_DIRECT
            putExtra("task_id", taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 1,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action button 2: Snooze 30 min
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_30_MIN
            putExtra("task_id", taskId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannelsManager.CHANNEL_REMINDERS)
            .setContentTitle("Discipline Checkpoint")
            .setContentText("$habitTitle is still pending before sleep cutoff.")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.checkbox_on_background, "Complete", completePendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 30m", snoozePendingIntent)
            .build()

        notificationManager.notify(taskId.hashCode(), notification)
    }

    companion object {
        const val ACTION_COMPLETE_TASK_DIRECT = "com.ascend75.action.COMPLETE_TASK_DIRECT"
        const val ACTION_SNOOZE_30_MIN = "com.ascend75.action.SNOOZE_30_MIN"
    }
}

package com.ascend75.core.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ascend75.core.notifications.ScheduleEvaluatorWorker

class TimezoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED || intent.action == Intent.ACTION_TIME_CHANGED) {
            val workRequest = OneTimeWorkRequestBuilder<ScheduleEvaluatorWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}

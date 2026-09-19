package com.ascend75.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannelsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createAllChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return

            val briefingChannel = NotificationChannel(
                CHANNEL_BRIEFING,
                "Daily Briefings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning protocol alerts and evening review briefings"
            }

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Intelligent Habit Nudges",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Smart, context-aware nudges for incomplete daily discipline habits"
                enableVibration(true)
            }

            val workoutChannel = NotificationChannel(
                CHANNEL_WORKOUT_TIMER,
                "Workout Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live ongoing status for active 45-minute workout sessions"
            }

            val milestoneChannel = NotificationChannel(
                CHANNEL_MILESTONES,
                "Milestones & Celebrations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Celebrations for completing Days 1, 7, 14, 21, 30, 45, 60, and 75"
                enableVibration(true)
            }

            manager.createNotificationChannels(
                listOf(briefingChannel, remindersChannel, workoutChannel, milestoneChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_BRIEFING = "channel_briefing"
        const val CHANNEL_REMINDERS = "channel_reminders"
        const val CHANNEL_WORKOUT_TIMER = "channel_workout_timer"
        const val CHANNEL_MILESTONES = "channel_milestones"
    }
}

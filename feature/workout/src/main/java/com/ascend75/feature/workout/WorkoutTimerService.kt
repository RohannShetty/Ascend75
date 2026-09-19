package com.ascend75.feature.workout

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutTimerService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _remainingSeconds = MutableStateFlow(45 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutTimerService = this@WorkoutTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Ascend75:WorkoutTimerWakeLock")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true
        wakeLock?.acquire(60 * 60 * 1000L) // 60 min max timeout safety

        val notification = buildNotification(_remainingSeconds.value)
        startForeground(NOTIFICATION_ID, notification)

        timerJob = serviceScope.launch {
            while (_isRunning.value && _remainingSeconds.value > 0) {
                delay(1000L)
                _remainingSeconds.value -= 1
                updateNotification(_remainingSeconds.value)
            }
            if (_remainingSeconds.value == 0) {
                _isRunning.value = false
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        if (wakeLock?.isHeld == true) wakeLock?.release()
        updateNotification(_remainingSeconds.value)
    }

    fun stopTimer() {
        pauseTimer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(remainingSec: Int): Notification {
        val minutes = remainingSec / 60
        val seconds = remainingSec % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ascend 75 Workout Session")
            .setContentText("Remaining: $timeString • Keep pushing")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(remainingSec: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(remainingSec))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Workout Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live ongoing chronometer for 45-minute discipline workouts"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock?.isHeld == true) wakeLock?.release()
        timerJob?.cancel()
    }

    companion object {
        const val CHANNEL_ID = "channel_workout_timer"
        const val NOTIFICATION_ID = 7501
        const val ACTION_START = "com.ascend75.action.START_WORKOUT"
        const val ACTION_PAUSE = "com.ascend75.action.PAUSE_WORKOUT"
        const val ACTION_STOP = "com.ascend75.action.STOP_WORKOUT"
    }
}

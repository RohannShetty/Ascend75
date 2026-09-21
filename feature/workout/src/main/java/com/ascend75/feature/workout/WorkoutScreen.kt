package com.ascend75.feature.workout

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendShapes
import com.ascend75.core.designsystem.theme.AscendTypography
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun WorkoutScreen(
    taskId: String,
    viewModel: WorkoutViewModel,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(taskId) {
        viewModel.initialize(taskId)
    }

    // The screen owns the foreground timer service: it binds for live state and drives it with
    // start/pause/stop commands, so the countdown on screen and the ongoing notification agree.
    DisposableEffect(context, scope) {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as? WorkoutTimerService.LocalBinder)?.getService() ?: return
                scope.launch {
                    service.remainingSeconds.collect { remaining ->
                        viewModel.updateTimer(remaining, service.isRunning.value)
                    }
                }
                scope.launch {
                    service.isRunning.collect { running ->
                        viewModel.updateTimer(service.remainingSeconds.value, running)
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        context.bindService(
            Intent(context, WorkoutTimerService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        onDispose {
            runCatching { context.unbindService(connection) }
        }
    }

    val startTimer: () -> Unit = {
        ContextCompat.startForegroundService(
            context,
            Intent(context, WorkoutTimerService::class.java).setAction(WorkoutTimerService.ACTION_START)
        )
    }
    // Pause/stop must not use startForegroundService: each call arms the "call startForeground
    // within 5s" watchdog, and these actions only update the already-running notification.
    val pauseTimer: () -> Unit = {
        context.startService(
            Intent(context, WorkoutTimerService::class.java).setAction(WorkoutTimerService.ACTION_PAUSE)
        )
    }
    val stopTimer: () -> Unit = {
        context.startService(
            Intent(context, WorkoutTimerService::class.java).setAction(WorkoutTimerService.ACTION_STOP)
        )
    }

    val timerText = remember(state.remainingSeconds) {
        val minutes = state.remainingSeconds / 60
        val seconds = state.remainingSeconds % 60
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SESSION TIMER",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.Primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "45-Minute Discipline",
                    style = AscendTypography.headlineMedium,
                    color = AscendPalette.OnSurface
                )
            }

            // Location Selector (Outdoor vs Indoor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(true to "Outdoor", false to "Indoor").forEach { (isOutdoor, label) ->
                    val selected = state.isOutdoor == isOutdoor
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) AscendPalette.PrimaryContainer else AscendPalette.SurfaceContainerHigh)
                            .clickable { viewModel.setOutdoor(isOutdoor) }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            style = AscendTypography.labelMedium,
                            color = if (selected) AscendPalette.OnPrimaryContainer else AscendPalette.OnSurfaceVariant
                        )
                    }
                }
            }

            // Central Circular Timer Display
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(AscendPalette.SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timerText,
                        style = AscendTypography.headlineLarge,
                        color = AscendPalette.OnSurface
                    )
                    Text(
                        text = if (state.isTimerRunning) "Active • Keep Moving" else "Paused",
                        style = AscendTypography.bodySmall,
                        color = if (state.isTimerRunning) AscendPalette.Success else AscendPalette.Warning
                    )
                }
            }

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!state.isTimerRunning) {
                    IconButton(
                        onClick = startTimer,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AscendPalette.Primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = AscendPalette.OnPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = pauseTimer,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AscendPalette.SurfaceContainerHighest)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = AscendPalette.OnSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                IconButton(
                    onClick = {
                        stopTimer()
                        viewModel.completeWorkout(taskId, onFinished)
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AscendPalette.SurfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Finish Workout",
                        tint = AscendPalette.Error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Early Finish Confirmation Dialog (<45 min)
        if (state.showEarlyFinishWarning) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissEarlyWarning() },
                shape = AscendShapes.extraLarge,
                containerColor = AscendPalette.SurfaceContainerHigh,
                title = {
                    Text(text = "Incomplete Duration", style = AscendTypography.headlineSmall, color = AscendPalette.Warning)
                },
                text = {
                    Text(
                        text = "Ascend 75 rules require a full 45 minutes for workout completion. Finishing early will log fewer than 45 minutes.",
                        style = AscendTypography.bodyMedium,
                        color = AscendPalette.OnSurfaceVariant
                    )
                },
                confirmButton = {
                    AscendButton(
                        text = "Log Anyway",
                        variant = AscendButtonVariant.Primary,
                        onClick = {
                            stopTimer()
                            viewModel.forceFinishWorkout(taskId, onFinished)
                        }
                    )
                },
                dismissButton = {
                    AscendButton(
                        text = "Resume Timer",
                        variant = AscendButtonVariant.Outline,
                        onClick = { viewModel.dismissEarlyWarning() }
                    )
                }
            )
        }

        // 3-Hour Separation Advisory Dialog
        if (state.showSeparationWarning) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSeparationWarning() },
                shape = AscendShapes.extraLarge,
                containerColor = AscendPalette.SurfaceContainerHigh,
                title = {
                    Text(text = "Rest Interval Notice", style = AscendTypography.headlineSmall, color = AscendPalette.Warning)
                },
                text = {
                    Text(
                        text = "Your first workout concluded less than 3 hours ago. For physiological recovery, separating sessions by at least 3 hours is strongly advised.",
                        style = AscendTypography.bodyMedium,
                        color = AscendPalette.OnSurfaceVariant
                    )
                },
                confirmButton = {
                    AscendButton(
                        text = "Understood",
                        variant = AscendButtonVariant.Primary,
                        onClick = { viewModel.dismissSeparationWarning() }
                    )
                }
            )
        }
    }
}

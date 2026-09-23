package com.ascend75.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascend75.core.domain.model.HabitType
import com.ascend75.core.domain.model.TaskEntry
import com.ascend75.core.designsystem.components.AscendLoadingGate
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun TrackersHubScreen(
    viewModel: TrackersHubViewModel,
    onOpenTracker: (TaskEntry) -> Unit,
    onOpenVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background
    ) { padding ->
        if (state.isLoading) {
            AscendLoadingGate(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column {
                        Text(
                            text = "DEDICATED ENGINES",
                            style = AscendTypography.labelSmall,
                            color = AscendPalette.Primary
                        )
                        Text(
                            text = "Trackers",
                            style = AscendTypography.headlineMedium,
                            color = AscendPalette.OnSurface
                        )
                        Text(
                            text = "Day ${state.dayNumber} of 75",
                            style = AscendTypography.bodySmall,
                            color = AscendPalette.OnSurfaceVariant
                        )
                    }
                }

                items(state.workouts, key = { task -> task.id }) { task ->
                    TrackerRow(
                        title = if (task.habitType == HabitType.WORKOUT_1) {
                            "Outdoor Workout (45m)"
                        } else {
                            "Second Workout (45m)"
                        },
                        subtitle = "Foreground timer • rest separation advisory",
                        isCompleted = task.isCompleted,
                        onClick = { onOpenTracker(task) }
                    )
                }

                state.water?.let { water ->
                    item(key = water.id) {
                        TrackerRow(
                            title = "Hydration",
                            subtitle = "${water.currentValue.toInt()} / ${water.targetValue.toInt()} ml",
                            isCompleted = water.isCompleted,
                            onClick = { onOpenTracker(water) }
                        )
                    }
                }

                state.reading?.let { reading ->
                    item(key = reading.id) {
                        TrackerRow(
                            title = "Reading",
                            subtitle = "${reading.currentValue.toInt()} / ${reading.targetValue.toInt()} pages",
                            isCompleted = reading.isCompleted,
                            onClick = { onOpenTracker(reading) }
                        )
                    }
                }

                state.photo?.let { photo ->
                    item(key = photo.id) {
                        TrackerRow(
                            title = "Progress Photo",
                            subtitle = "Hardware-encrypted vault",
                            isCompleted = photo.isCompleted,
                            onClick = onOpenVault
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TrackerRow(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AscendTypography.headlineSmall,
                    color = AscendPalette.OnSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AscendTypography.bodySmall,
                    color = AscendPalette.OnSurfaceVariant
                )
            }
            Text(
                text = if (isCompleted) "Complete" else "Open →",
                style = AscendTypography.labelMedium,
                color = if (isCompleted) AscendPalette.Success else AscendPalette.Primary
            )
        }
    }
}

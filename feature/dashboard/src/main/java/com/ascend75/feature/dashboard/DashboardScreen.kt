package com.ascend75.feature.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.components.AscendProgressRing
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.components.HabitCheckCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography
import com.ascend75.feature.dashboard.components.DayResetDialog
import com.ascend75.feature.dashboard.components.TaskDetailBottomSheet

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToScienceLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AscendPalette.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Header with Day Counter & Mode Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAY ${state.dayNumber} OF 75",
                                style = AscendTypography.headlineLarge,
                                color = AscendPalette.OnSurface
                            )
                            Text(
                                text = "${state.mode.title} • Sleep Cutoff 3:00 AM",
                                style = AscendTypography.bodySmall,
                                color = AscendPalette.OnSurfaceVariant
                            )
                        }

                        // Streak Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AscendPalette.SurfaceContainerHigh)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⚡ ${state.streakDays}d Streak",
                                style = AscendTypography.labelMedium,
                                color = AscendPalette.Primary
                            )
                        }
                    }
                }

                // Centered Progress Ring
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AscendProgressRing(
                            totalSegments = state.totalCount,
                            completedSegments = state.completedCount,
                            ringSize = 200.dp
                        )
                    }
                }

                // Science Card Highlight of the Day
                item {
                    GlassCard(
                        containerColor = AscendPalette.SurfaceContainerHigh.copy(alpha = 0.5f),
                        borderColor = AscendPalette.SecondaryContainer.copy(alpha = 0.3f),
                        onClick = onNavigateToScienceLibrary
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DAY ${state.dayNumber} PROTOCOL",
                                    style = AscendTypography.labelSmall,
                                    color = AscendPalette.Secondary
                                )
                                Text(
                                    text = state.dailyScienceCardTitle ?: "Circadian Biology Protocol",
                                    style = AscendTypography.labelLarge,
                                    color = AscendPalette.OnSurface
                                )
                            }
                            Text(
                                text = "Read →",
                                style = AscendTypography.labelMedium,
                                color = AscendPalette.Secondary
                            )
                        }
                    }
                }

                // Daily Habit Checklist
                item {
                    Text(
                        text = "Daily Guardrails (${state.completedCount}/${state.totalCount})",
                        style = AscendTypography.labelLarge,
                        color = AscendPalette.Primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(state.tasks, key = { it.id }) { task ->
                    val (title, category, subtitle) = when (task.habitType) {
                        "WORKOUT_1" -> Triple("Outdoor Workout (45m)", "Physical Discipline", "Mandatory outdoor session")
                        "WORKOUT_2" -> Triple("Second Workout (45m)", "Physical Discipline", "Separated by 3+ hours")
                        "WATER" -> Triple("Hydration (${task.targetValue.toInt()} ml)", "Physiological Fuel", "${task.currentValue.toInt()} ml logged")
                        "READING" -> Triple("Read 10 Pages", "Cognitive Growth", "Non-fiction / personal development")
                        "DIET" -> Triple("Strict Clean Diet", "Nutrition Integrity", "Zero alcohol, zero cheat meals")
                        "PHOTO" -> Triple("Progress Photo", "Visual Accountability", "Hardware-encrypted vault")
                        else -> Triple("Custom Habit", "Daily Practice", "Discipline requirement")
                    }

                    HabitCheckCard(
                        title = title,
                        category = category,
                        subtitle = subtitle,
                        isCompleted = task.isCompleted,
                        onToggle = { isChecked ->
                            viewModel.toggleTaskCompletion(task.id, isChecked)
                        },
                        onClick = {
                            viewModel.selectTaskForDetail(task)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Task Detail Bottom Sheet
            state.selectedTaskForDetail?.let { task ->
                TaskDetailBottomSheet(
                    task = task,
                    onDismiss = { viewModel.selectTaskForDetail(null) },
                    onToggleCompletion = { isCompleted ->
                        viewModel.toggleTaskCompletion(task.id, isCompleted)
                    },
                    onSaveNotes = { notes ->
                        viewModel.saveTaskNotes(task.id, notes)
                    }
                )
            }

            // Day Reset Dialog for Strict Mode failures past sleep cutoff
            if (state.showResetDialog) {
                DayResetDialog(
                    dayNumber = state.dayNumber,
                    completedTasks = state.completedCount,
                    totalTasks = state.totalCount,
                    onConfirmReset = { reflection, switchToFlexible ->
                        viewModel.archiveAndResetStrictAttempt(reflection, switchToFlexible)
                    }
                )
            }
        }
    }
}

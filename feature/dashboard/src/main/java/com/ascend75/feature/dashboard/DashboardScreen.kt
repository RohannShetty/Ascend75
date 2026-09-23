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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascend75.core.domain.model.HabitType
import com.ascend75.core.domain.model.TaskEntry
import com.ascend75.core.designsystem.components.AscendProgressRing
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.components.HabitCheckCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendShapesTokens
import com.ascend75.core.designsystem.theme.AscendTypography
import com.ascend75.feature.dashboard.components.DayResetDialog
import com.ascend75.feature.dashboard.components.MilestoneCelebrationDialog
import com.ascend75.feature.dashboard.components.TaskDetailBottomSheet
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenTracker: (TaskEntry) -> Unit,
    onNavigateToScienceLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background
    ) { padding ->
        if (state.isLoading) {
            DashboardSkeleton(
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
                                text = remember(state.mode, state.sleepCutoffHour, state.sleepCutoffMinute) {
                                    "${state.mode.title} • Sleep Cutoff ${formatSleepCutoff(state.sleepCutoffHour, state.sleepCutoffMinute)}"
                                },
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

                state.errorMessage?.let { message ->
                    item {
                        GlassCard(
                            containerColor = AscendPalette.ErrorContainer.copy(alpha = 0.2f),
                            borderColor = AscendPalette.Error.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = message,
                                style = AscendTypography.bodyMedium,
                                color = AscendPalette.Error
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
                    val presentation = remember(task.habitType, task.targetValue, task.currentValue) {
                        habitPresentation(task.habitType, task.targetValue, task.currentValue)
                    }

                    HabitCheckCard(
                        title = presentation.title,
                        category = presentation.category,
                        subtitle = presentation.subtitle,
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
                    },
                    onOpenTracker = if (task.habitType == HabitType.DIET) null else { { onOpenTracker(task) } }
                )
            }

            // Milestone Celebration
            if (state.showMilestoneDialog) {
                MilestoneCelebrationDialog(
                    dayNumber = state.dayNumber,
                    onDismiss = { viewModel.dismissMilestone() }
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

/**
 * First-paint placeholder matching the dashboard's header / ring / list heights so the real frame
 * does not shift the layout when it arrives.
 */
@Composable
private fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp).padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SkeletonBar(height = 64.dp)
        SkeletonBar(height = 224.dp)
        SkeletonBar(height = 72.dp)
        SkeletonBar(height = 96.dp)
        SkeletonBar(height = 96.dp)
    }
}

@Composable
private fun SkeletonBar(height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(AscendShapesTokens.ExtraLarge))
            .background(AscendPalette.SurfaceContainerLow)
    )
}

/** Immutable presentation holder so list items do not allocate interpolated strings per recomposition. */
private data class HabitPresentation(
    val title: String,
    val category: String,
    val subtitle: String
)

private fun habitPresentation(habitType: HabitType, targetValue: Double, currentValue: Double): HabitPresentation =
    when (habitType) {
        HabitType.WORKOUT_1 -> HabitPresentation("Outdoor Workout (45m)", "Physical Discipline", "Mandatory outdoor session")
        HabitType.WORKOUT_2 -> HabitPresentation("Second Workout (45m)", "Physical Discipline", "Separated by 3+ hours")
        HabitType.WATER -> HabitPresentation(
            "Hydration (${targetValue.toInt()} ml)",
            "Physiological Fuel",
            "${currentValue.toInt()} ml logged"
        )
        HabitType.READING -> HabitPresentation("Read 10 Pages", "Cognitive Growth", "Non-fiction / personal development")
        HabitType.DIET -> HabitPresentation("Strict Clean Diet", "Nutrition Integrity", "Zero alcohol, zero cheat meals")
        HabitType.PHOTO -> HabitPresentation("Progress Photo", "Visual Accountability", "Hardware-encrypted vault")
        HabitType.CUSTOM -> HabitPresentation("Custom Habit", "Daily Practice", "Discipline requirement")
    }

private fun formatSleepCutoff(hour: Int, minute: Int): String {
    val normalized = ((hour % 24) + 24) % 24
    val displayHour = when {
        normalized == 0 -> 12
        normalized > 12 -> normalized - 12
        else -> normalized
    }
    val suffix = if (normalized < 12) "AM" else "PM"
    return String.format(Locale.US, "%d:%02d %s", displayHour, minute, suffix)
}

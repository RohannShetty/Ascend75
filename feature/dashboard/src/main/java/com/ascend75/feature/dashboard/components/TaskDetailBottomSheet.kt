package com.ascend75.feature.dashboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailBottomSheet(
    task: TaskEntryEntity,
    onDismiss: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit,
    onSaveNotes: (String) -> Unit,
    onOpenTracker: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var notesText by remember { mutableStateOf(task.notes ?: "") }

    val (title, rationale) = when (task.habitType) {
        "WORKOUT_1" -> Pair("Outdoor Workout (45m)", "Circadian optical stimulation, non-negotiable mental resilience, and metabolic activation independent of weather conditions.")
        "WORKOUT_2" -> Pair("Second Workout (45m)", "Elevated energy expenditure, active muscle recovery, and deliberate pacing separated by 3+ hours.")
        "WATER" -> Pair("Hydration Intake (3.8L)", "Optimal cellular volume, kidney clearance, and cognitive endurance. Paced evenly across waking hours.")
        "READING" -> Pair("10 Pages Non-Fiction", "Continuous neuroplasticity, deep cognitive focus, and deliberate information synthesis.")
        "DIET" -> Pair("Strict Clean Diet", "Sustained insulin stability, reduced systemic inflammation, and complete alcohol elimination.")
        "PHOTO" -> Pair("Progress Photo", "Visual accountability, private hardware-encrypted tracking, and objective body composition monitoring.")
        else -> Pair("Custom Discipline Habit", "Consistent intentional behavior repeated daily.")
    }

    val trackerLabel = when (task.habitType) {
        "WORKOUT_1", "WORKOUT_2" -> "Start Workout"
        "WATER" -> "Log Water"
        "READING" -> "Log Reading"
        "PHOTO" -> "Capture Photo"
        else -> null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AscendPalette.SurfaceContainerHigh,
        contentColor = AscendPalette.OnSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = task.habitType.uppercase(),
                style = AscendTypography.labelSmall,
                color = AscendPalette.Primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = AscendTypography.headlineMedium,
                color = AscendPalette.OnSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(containerColor = AscendPalette.SurfaceContainer) {
                Column {
                    Text(
                        text = "PHYSIOLOGICAL RATIONALE",
                        style = AscendTypography.labelSmall,
                        color = AscendPalette.Secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rationale,
                        style = AscendTypography.bodyMedium,
                        color = AscendPalette.OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Session Reflection & Notes:",
                style = AscendTypography.labelMedium,
                color = AscendPalette.OnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Add notes (intensity, weather, book title, feelings)...",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AscendPalette.Primary,
                    unfocusedBorderColor = AscendPalette.Outline.copy(alpha = 0.3f)
                ),
                minLines = 2,
                maxLines = 4
            )

            val trackerAction = onOpenTracker
            if (trackerAction != null && trackerLabel != null) {
                Spacer(modifier = Modifier.height(20.dp))
                AscendButton(
                    text = trackerLabel,
                    variant = AscendButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDismiss()
                        trackerAction()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AscendButton(
                    text = "Save Notes",
                    variant = AscendButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                    onClick = { onSaveNotes(notesText) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                AscendButton(
                    text = if (task.isCompleted) "Mark Incomplete" else "Mark Complete",
                    variant = if (task.isCompleted) AscendButtonVariant.Outline else AscendButtonVariant.Primary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onToggleCompletion(!task.isCompleted)
                        onDismiss()
                    }
                )
            }
        }
    }
}

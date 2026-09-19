package com.ascend75.feature.dashboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendShapes
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun DayResetDialog(
    dayNumber: Int,
    completedTasks: Int,
    totalTasks: Int,
    onConfirmReset: (reflection: String, switchToFlexible: Boolean) -> Unit
) {
    var reflectionText by remember { mutableStateOf("") }
    var isSwitchingToFlexible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* Non-dismissable without conscious choice */ },
        shape = AscendShapes.extraLarge,
        containerColor = AscendPalette.SurfaceContainerHigh,
        title = {
            Column {
                Text(
                    text = "CHECKPOINT & RESET",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.Warning
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Day $dayNumber Unfinished",
                    style = AscendTypography.headlineMedium,
                    color = AscendPalette.OnSurface
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "In Strict 75, crossing the sleep cutoff with $completedTasks of $totalTasks habits completed requires archiving this attempt and starting fresh. Your historical logs, photos, and workouts remain permanently preserved.",
                    style = AscendTypography.bodyMedium,
                    color = AscendPalette.OnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Qualitative Reflection (What will you adjust?):",
                    style = AscendTypography.labelMedium,
                    color = AscendPalette.OnSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "e.g. Work meeting ran late; scheduling workout before 10 AM.",
                            style = AscendTypography.bodySmall,
                            color = AscendPalette.OnSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AscendPalette.Primary,
                        unfocusedBorderColor = AscendPalette.Outline.copy(alpha = 0.4f)
                    ),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                AscendButton(
                    text = "Archive & Restart Day 1",
                    variant = AscendButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onConfirmReset(reflectionText, false) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                AscendButton(
                    text = "Switch to Flexible 75",
                    variant = AscendButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onConfirmReset(reflectionText, true) }
                )
            }
        },
        dismissButton = null
    )
}

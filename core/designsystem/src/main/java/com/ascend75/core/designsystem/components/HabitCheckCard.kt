package com.ascend75.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTheme
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun HabitCheckCard(
    title: String,
    category: String,
    subtitle: String,
    isCompleted: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    val checkColor by animateColorAsState(
        targetValue = if (isCompleted) AscendPalette.Success else AscendPalette.SurfaceContainerHighest,
        label = "checkColorAnim"
    )

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Category Chip Tag
                Text(
                    text = category.uppercase(),
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.Primary
                )
                Text(
                    text = title,
                    style = AscendTypography.headlineSmall,
                    color = AscendPalette.OnSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = subtitle,
                    style = AscendTypography.bodySmall,
                    color = AscendPalette.OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Checkbox Circle Toggle Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) AscendPalette.Success else AscendPalette.SurfaceContainerHigh)
                    .border(
                        width = 1.5.dp,
                        color = if (isCompleted) AscendPalette.Success else AscendPalette.Outline.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle(!isCompleted)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = AscendPalette.OnSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F131C)
@Composable
private fun HabitCheckCardPreview() {
    AscendTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            HabitCheckCard(
                title = "Outdoor Workout (45m)",
                category = "Physical Discipline",
                subtitle = "No excuses, regardless of weather",
                isCompleted = false,
                onToggle = {}
            )
        }
    }
}

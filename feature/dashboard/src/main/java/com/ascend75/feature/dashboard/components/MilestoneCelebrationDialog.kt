package com.ascend75.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendShapes
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun MilestoneCelebrationDialog(
    dayNumber: Int,
    onDismiss: () -> Unit
) {
    val (title, subtitle) = when (dayNumber) {
        1 -> Pair("Baseline Established", "You have taken the first definitive step into intentional discipline. Day 1 is in the books.")
        7 -> Pair("One Week of Steel", "7 consecutive days of zero compromise. Your circadian rhythms and habit loops are beginning to wire.")
        14 -> Pair("Fortnight of Tenacity", "Two full weeks. Cognitive resistance is peaking; push through this critical neuroplastic threshold.")
        21 -> Pair("Habit Stabilization", "21 days completed. Corticostriatal pathways are solidifying automatic action patterns.")
        30 -> Pair("One Month Ascended", "30 days of uncompromising execution. 60 workouts, hundreds of liters, and deep cognitive expansion.")
        45 -> Pair("The 60% Horizon", "Past the halfway mark. Discipline is no longer an effort—it is becoming your default operating state.")
        60 -> Pair("Executive Resilience", "60 days of continuous focus. You have surpassed the 66-day average threshold of neurochemical automaticity.")
        75 -> Pair("Ascension Achieved", "75 days. Complete transformation of mind, physique, and sovereign autonomy. You have conquered the ascent.")
        else -> Pair("Milestone Reached", "Another pivotal benchmark cleared on your 75-day journey.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AscendShapes.extraLarge,
        containerColor = AscendPalette.SurfaceContainerHigh,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AscendPalette.PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "★",
                        style = AscendTypography.headlineLarge,
                        color = AscendPalette.OnPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "DAY $dayNumber MILESTONE",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.Primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = AscendTypography.headlineMedium,
                    color = AscendPalette.OnSurface
                )
            }
        },
        text = {
            Text(
                text = subtitle,
                style = AscendTypography.bodyMedium,
                color = AscendPalette.OnSurfaceVariant
            )
        },
        confirmButton = {
            AscendButton(
                text = "Continue the Ascent",
                variant = AscendButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss
            )
        },
        dismissButton = null
    )
}

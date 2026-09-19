package com.ascend75.feature.water

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendShapes
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun WaterScreen(
    taskId: String,
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    val fraction = (state.currentTotalMl.toFloat() / state.targetMl.toFloat()).coerceIn(0f, 1f)
    val animatedFill by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "waterFillAnim"
    )

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
                    text = "HYDRATION HUB",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.Secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.targetMl} ml Target",
                    style = AscendTypography.headlineMedium,
                    color = AscendPalette.OnSurface
                )
            }

            // Circular Water Gauge
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(AscendPalette.SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                // Bottom-to-top fluid fill simulator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(240.dp * animatedFill)
                            .background(AscendPalette.SecondaryContainer.copy(alpha = 0.35f))
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.currentTotalMl} ml",
                        style = AscendTypography.headlineLarge,
                        color = AscendPalette.OnSurface
                    )
                    Text(
                        text = "${(fraction * 100).toInt()}% of goal",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.Secondary
                    )
                }
            }

            // Quick-Add Volume Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "QUICK ADD INTAKE",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(250, 500, 750, 1000).forEach { amount ->
                        AscendButton(
                            text = "+$amount ml",
                            variant = AscendButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.logWater(amount, taskId) }
                        )
                    }
                }
            }

            // Hourly Pacing Advisory Card
            GlassCard(containerColor = AscendPalette.SurfaceContainerHigh.copy(alpha = 0.4f)) {
                Column {
                    Text(
                        text = "PACED HYDRATION GUIDELINE",
                        style = AscendTypography.labelSmall,
                        color = AscendPalette.Secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aim for ~250–350 ml per waking hour. Never consume more than 1.0–1.2 L in an hour to protect your renal system and maintain sodium-potassium balance.",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant
                    )
                }
            }
        }

        // Hyponatremia Rate Warning Dialog
        if (state.showHyponatremiaWarning) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissWarning() },
                shape = AscendShapes.extraLarge,
                containerColor = AscendPalette.SurfaceContainerHigh,
                title = {
                    Text(
                        text = "Rapid Fluid Intake Warning",
                        style = AscendTypography.headlineSmall,
                        color = AscendPalette.Warning
                    )
                },
                text = {
                    Text(
                        text = "You are logging over 1,200 ml within a 60-minute window. Rapid fluid consumption without electrolytes can cause dangerous hyponatremia (water intoxication). Please sip slowly.",
                        style = AscendTypography.bodyMedium,
                        color = AscendPalette.OnSurfaceVariant
                    )
                },
                confirmButton = {
                    AscendButton(
                        text = "Pace Myself",
                        variant = AscendButtonVariant.Primary,
                        onClick = { viewModel.dismissWarning() }
                    )
                },
                dismissButton = {
                    AscendButton(
                        text = "Log Anyway",
                        variant = AscendButtonVariant.Outline,
                        onClick = { viewModel.logWater(250, taskId, forceLog = true) }
                    )
                }
            )
        }
    }
}

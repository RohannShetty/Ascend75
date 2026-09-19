package com.ascend75.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendShapes
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToOnboardingAfterWipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Text(
                    text = "VAULT & PREFERENCES",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.Primary
                )
                Text(
                    text = "System Settings",
                    style = AscendTypography.headlineMedium,
                    color = AscendPalette.OnSurface
                )
            }

            // Security Settings Card
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "SECURITY & BIOMETRICS", style = AscendTypography.labelSmall, color = AscendPalette.Primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Hardware Biometric Gate", style = AscendTypography.headlineSmall, color = AscendPalette.OnSurface)
                            Text(
                                text = "Require fingerprint/PIN before revealing transformation photos",
                                style = AscendTypography.bodySmall,
                                color = AscendPalette.OnSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.userPreferences.isBiometricEnabled,
                            onCheckedChange = viewModel::setBiometricEnabled,
                            colors = SwitchDefaults.colors(checkedThumbColor = AscendPalette.Primary)
                        )
                    }
                }
            }

            // Day Boundary & Schedule
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "DAY BOUNDARY", style = AscendTypography.labelSmall, color = AscendPalette.Secondary)
                    Text(
                        text = "Sleep Cutoff: ${String.format("%02d:%02d AM", state.userPreferences.sleepCutoffHour, state.userPreferences.sleepCutoffMinute)}",
                        style = AscendTypography.headlineSmall,
                        color = AscendPalette.OnSurface
                    )
                    Text(
                        text = "Habits completed before this cutoff timestamp credit toward the previous day.",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant
                    )
                }
            }

            // Data Export
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "DATA SOVEREIGNTY & EXPORT", style = AscendTypography.labelSmall, color = AscendPalette.Primary)
                    Text(
                        text = "Export all challenge history, workout timestamps, and hydration logs into portable JSON.",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant
                    )
                    AscendButton(
                        text = if (state.isExporting) "Generating..." else "Export JSON Archive",
                        variant = AscendButtonVariant.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::exportData
                    )
                    state.exportedJson?.let {
                        Text(
                            text = "Archive generated (${it.length} bytes ready to save)",
                            style = AscendTypography.bodySmall,
                            color = AscendPalette.Success
                        )
                    }
                }
            }

            // Danger Zone: Data Wipe
            GlassCard(
                containerColor = AscendPalette.ErrorContainer.copy(alpha = 0.15f),
                borderColor = AscendPalette.Error.copy(alpha = 0.3f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "DANGER ZONE", style = AscendTypography.labelSmall, color = AscendPalette.Error)
                    Text(
                        text = "Permanently zero-fill and delete all encrypted progress photos, truncate all Room databases, and reset the app.",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant
                    )
                    AscendButton(
                        text = "Cryptographic Data Wipe",
                        variant = AscendButtonVariant.Outline,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::promptDataWipe
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Wipe Confirmation Dialog
        if (state.showWipeConfirmation) {
            AlertDialog(
                onDismissRequest = viewModel::dismissWipeConfirmation,
                shape = AscendShapes.extraLarge,
                containerColor = AscendPalette.SurfaceContainerHigh,
                title = {
                    Text(text = "Confirm Sovereign Data Erasure", style = AscendTypography.headlineSmall, color = AscendPalette.Error)
                },
                text = {
                    Text(
                        text = "This action is irreversible. All encrypted photos will be overwritten with cryptographic zeros before deletion, and all challenge logs will be permanently erased.",
                        style = AscendTypography.bodyMedium,
                        color = AscendPalette.OnSurfaceVariant
                    )
                },
                confirmButton = {
                    AscendButton(
                        text = "Permanently Wipe Everything",
                        variant = AscendButtonVariant.Primary,
                        onClick = { viewModel.executeDataWipe(onNavigateToOnboardingAfterWipe) }
                    )
                },
                dismissButton = {
                    AscendButton(
                        text = "Cancel",
                        variant = AscendButtonVariant.Secondary,
                        onClick = viewModel::dismissWipeConfirmation
                    )
                }
            )
        }
    }
}

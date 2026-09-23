package com.ascend75.feature.settings

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendShapes
import com.ascend75.core.designsystem.theme.AscendTypography
import java.util.Locale

private const val MEDICAL_DISCLAIMER =
    "Health Notice: Ascend 75 is an educational discipline and habit development tool. It is not a " +
        "medical device. Always consult a qualified physician prior to initiating strenuous fitness " +
        "regimens or major hydration shifts."

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToOnboardingAfterWipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                    text = "More",
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
                        text = remember(
                            state.userPreferences.sleepCutoffHour,
                            state.userPreferences.sleepCutoffMinute
                        ) {
                            String.format(
                                Locale.US,
                                "Sleep Cutoff: %02d:%02d",
                                state.userPreferences.sleepCutoffHour,
                                state.userPreferences.sleepCutoffMinute
                            )
                        },
                        style = AscendTypography.headlineSmall,
                        color = AscendPalette.OnSurface
                    )
                    Slider(
                        value = state.userPreferences.sleepCutoffHour.toFloat(),
                        onValueChange = { viewModel.setSleepCutoff(it.toInt(), 0) },
                        valueRange = 0f..6f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = AscendPalette.Primary,
                            activeTrackColor = AscendPalette.Primary
                        )
                    )
                    Text(
                        text = "Habits completed before this cutoff timestamp credit toward the previous day.",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant
                    )
                }
            }

            // Protocol Mode
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "PROTOCOL MODE", style = AscendTypography.labelSmall, color = AscendPalette.Primary)
                    Text(
                        text = "Applies from the next day boundary.",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant
                    )

                    val selectedMode = ChallengeMode.fromString(state.userPreferences.selectedMode)
                    ChallengeMode.entries.chunked(2).forEach { rowModes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowModes.forEach { mode ->
                                ModeChip(
                                    title = mode.title,
                                    isSelected = mode == selectedMode,
                                    onClick = { viewModel.setMode(mode) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowModes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
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
                        enabled = !state.isExporting,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::exportData
                    )
                    state.exportedFile?.let { file ->
                        Text(
                            text = "Archive ready (${file.length() / 1024} KB)",
                            style = AscendTypography.bodySmall,
                            color = AscendPalette.Success
                        )
                        AscendButton(
                            text = "Share Archive",
                            variant = AscendButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Ascend 75 archive"))
                            }
                        )
                    }
                    state.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = AscendTypography.bodySmall,
                            color = AscendPalette.Error
                        )
                    }
                }
            }

            // About & Disclaimer
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "ABOUT", style = AscendTypography.labelSmall, color = AscendPalette.Secondary)
                    Text(
                        text = "Ascend 75${if (state.appVersion.isNotBlank()) " v${state.appVersion}" else ""}",
                        style = AscendTypography.headlineSmall,
                        color = AscendPalette.OnSurface
                    )
                    Text(
                        text = "Offline-first. No accounts, no telemetry, no network calls.",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MEDICAL DISCLAIMER",
                        style = AscendTypography.labelSmall,
                        color = AscendPalette.Warning
                    )
                    Text(
                        text = MEDICAL_DISCLAIMER,
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant
                    )
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
                        text = "This action is irreversible. All encrypted photos will be overwritten with cryptographic zeros before deletion, the vault master key is destroyed, and all challenge logs will be permanently erased.",
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

@Composable
private fun ModeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) AscendPalette.PrimaryContainer else AscendPalette.SurfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = AscendTypography.labelMedium,
            color = if (isSelected) AscendPalette.OnPrimaryContainer else AscendPalette.OnSurfaceVariant,
            maxLines = 1
        )
    }
}

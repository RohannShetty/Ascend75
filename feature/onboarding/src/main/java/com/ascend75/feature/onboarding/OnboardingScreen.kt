package com.ascend75.feature.onboarding

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography
import java.util.Locale

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = state.currentStep > 1) {
        viewModel.previousStep()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.currentStep > 1) {
                    AscendButton(
                        text = "Back",
                        variant = AscendButtonVariant.Secondary,
                        onClick = { viewModel.previousStep() }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (state.currentStep < state.totalSteps) {
                    AscendButton(
                        text = "Continue",
                        variant = AscendButtonVariant.Primary,
                        onClick = { viewModel.nextStep() }
                    )
                } else {
                    AscendButton(
                        text = if (state.isCompleting) "Initializing..." else "Begin Day 1",
                        variant = AscendButtonVariant.Primary,
                        enabled = !state.isCompleting,
                        onClick = { viewModel.completeOnboarding(onFinish) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step Indicator Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP ${state.currentStep} OF ${state.totalSteps}",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.Primary
                )
                Text(
                    text = "ASCEND 75",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AscendPalette.SurfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = state.currentStep.toFloat() / state.totalSteps.toFloat())
                        .height(4.dp)
                        .background(AscendPalette.Primary)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error banner if any
            state.errorMessage?.let { errorMsg ->
                GlassCard(
                    containerColor = AscendPalette.ErrorContainer.copy(alpha = 0.2f),
                    borderColor = AscendPalette.Error.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMsg,
                        style = AscendTypography.bodyMedium,
                        color = AscendPalette.Error
                    )
                }
            }

            // Step Content
            when (state.currentStep) {
                1 -> WelcomeStep(state = state, onAcceptDisclaimer = viewModel::setDisclaimerAccepted)
                2 -> GoalsStep(state = state, onFitnessChange = viewModel::setFitnessLevel, onMotivationChange = viewModel::setPrimaryMotivation)
                3 -> ScheduleStep(state = state, onCutoffChange = viewModel::setSleepCutoff)
                4 -> ModeStep(state = state, onModeSelected = viewModel::setSelectedMode)
                5 -> TargetsStep(state = state, onWaterChange = viewModel::setWaterTarget)
                6 -> SecurityStep(state = state, onBiometricToggle = viewModel::setBiometricLock)
            }

            if (state.isCompleting) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AscendPalette.Primary)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun WelcomeStep(
    state: OnboardingUiState,
    onAcceptDisclaimer: (Boolean) -> Unit
) {
    Text(
        text = "Ascend 75 — Discipline Operating System",
        style = AscendTypography.headlineMedium,
        color = AscendPalette.OnSurface
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Ascend 75 is a science-grounded, offline-first personal challenge engineered to forge mental endurance and circadian alignment.",
        style = AscendTypography.bodyMedium,
        color = AscendPalette.OnSurfaceVariant
    )

    Spacer(modifier = Modifier.height(20.dp))

    GlassCard(borderColor = AscendPalette.Warning.copy(alpha = 0.3f)) {
        Column {
            Text(
                text = "MEDICAL & SAFETY NOTICE",
                style = AscendTypography.labelSmall,
                color = AscendPalette.Warning
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ascend 75 is an educational habit platform, not a medical or clinical fitness tool. Consult a qualified physician before undertaking rigorous exercise or drastic hydration changes. Never train through sharp injury or extreme conditions.",
                style = AscendTypography.bodySmall,
                color = AscendPalette.OnSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAcceptDisclaimer(!state.isDisclaimerAccepted) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = state.isDisclaimerAccepted,
            onCheckedChange = onAcceptDisclaimer,
            colors = CheckboxDefaults.colors(checkedColor = AscendPalette.Primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "I have read and accept the health & safety disclaimers.",
            style = AscendTypography.bodyMedium,
            color = AscendPalette.OnSurface
        )
    }
}

@Composable
private fun GoalsStep(
    state: OnboardingUiState,
    onFitnessChange: (String) -> Unit,
    onMotivationChange: (String) -> Unit
) {
    Text(
        text = "Personal Baseline & Focus",
        style = AscendTypography.headlineMedium,
        color = AscendPalette.OnSurface
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(text = "Current Conditioning", style = AscendTypography.labelLarge, color = AscendPalette.Primary)
    Spacer(modifier = Modifier.height(8.dp))
    listOf("Beginner", "Intermediate", "Advanced / Athlete").forEach { level ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFitnessChange(level) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = state.fitnessLevel == level,
                onClick = { onFitnessChange(level) },
                colors = RadioButtonDefaults.colors(selectedColor = AscendPalette.Primary)
            )
            Text(text = level, style = AscendTypography.bodyMedium, color = AscendPalette.OnSurface)
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "Primary Motivation", style = AscendTypography.labelLarge, color = AscendPalette.Primary)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = state.primaryMotivation,
        onValueChange = onMotivationChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "e.g. Mental toughness & circadian alignment",
                style = AscendTypography.bodySmall,
                color = AscendPalette.OnSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AscendPalette.Primary,
            unfocusedBorderColor = AscendPalette.Outline.copy(alpha = 0.3f)
        ),
        minLines = 2,
        maxLines = 3
    )
}

@Composable
private fun ScheduleStep(
    state: OnboardingUiState,
    onCutoffChange: (Int, Int) -> Unit
) {
    Text(
        text = "Sleep Cutoff Boundary",
        style = AscendTypography.headlineMedium,
        color = AscendPalette.OnSurface
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Unlike apps that reset strictly at midnight, Ascend 75 calculates your day boundary around your actual sleep schedule. Tasks finished before cutoff count toward today.",
        style = AscendTypography.bodyMedium,
        color = AscendPalette.OnSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    GlassCard {
        Column {
            Text(
                text = "DAY END CUTOFF",
                style = AscendTypography.labelSmall,
                color = AscendPalette.Primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = remember(state.sleepCutoffHour, state.sleepCutoffMinute) {
                    String.format(Locale.US, "%02d:%02d AM", state.sleepCutoffHour, state.sleepCutoffMinute)
                },
                style = AscendTypography.headlineLarge,
                color = AscendPalette.OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = state.sleepCutoffHour.toFloat(),
                onValueChange = { hour -> onCutoffChange(hour.toInt(), 0) },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(thumbColor = AscendPalette.Primary, activeTrackColor = AscendPalette.Primary)
            )
            Text(
                text = "Recommended: 3:00 AM gives ample cushion for late evening reading or reflection without premature failure.",
                style = AscendTypography.bodySmall,
                color = AscendPalette.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModeStep(
    state: OnboardingUiState,
    onModeSelected: (ChallengeMode) -> Unit
) {
    Text(
        text = "Select Challenge Mode",
        style = AscendTypography.headlineMedium,
        color = AscendPalette.OnSurface
    )
    Spacer(modifier = Modifier.height(16.dp))

    ChallengeMode.entries.forEach { mode ->
        val isSelected = state.selectedMode == mode
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            borderColor = if (isSelected) AscendPalette.Primary else AscendPalette.Outline.copy(alpha = 0.2f),
            containerColor = if (isSelected) AscendPalette.SurfaceContainerHigh else AscendPalette.SurfaceContainerLow,
            onClick = { onModeSelected(mode) }
        ) {
            Column {
                Text(
                    text = mode.title,
                    style = AscendTypography.headlineSmall,
                    color = if (isSelected) AscendPalette.Primary else AscendPalette.OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mode.description,
                    style = AscendTypography.bodySmall,
                    color = AscendPalette.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TargetsStep(
    state: OnboardingUiState,
    onWaterChange: (Int) -> Unit
) {
    Text(
        text = "Custom Targets",
        style = AscendTypography.headlineMedium,
        color = AscendPalette.OnSurface
    )
    Spacer(modifier = Modifier.height(16.dp))

    GlassCard {
        Column {
            Text(text = "DAILY HYDRATION GOAL", style = AscendTypography.labelSmall, color = AscendPalette.Primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = remember(state.waterTargetMl) {
                    String.format(Locale.US, "%d ml (%.1f Liters)", state.waterTargetMl, state.waterTargetMl / 1000.0)
                },
                style = AscendTypography.headlineSmall,
                color = AscendPalette.OnSurface
            )
            Slider(
                value = state.waterTargetMl.toFloat(),
                onValueChange = { onWaterChange((it / 100).toInt() * 100) },
                valueRange = 2000f..4500f,
                steps = 24,
                colors = SliderDefaults.colors(thumbColor = AscendPalette.Secondary, activeTrackColor = AscendPalette.Secondary)
            )
        }
    }
}

@Composable
private fun SecurityStep(
    state: OnboardingUiState,
    onBiometricToggle: (Boolean) -> Unit
) {
    Text(
        text = "Privacy & Photo Vault",
        style = AscendTypography.headlineMedium,
        color = AscendPalette.OnSurface
    )
    Spacer(modifier = Modifier.height(16.dp))

    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hardware Biometric Gate",
                    style = AscendTypography.headlineSmall,
                    color = AscendPalette.OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Require fingerprint, face, or device PIN before displaying sensitive transformation photos.",
                    style = AscendTypography.bodySmall,
                    color = AscendPalette.OnSurfaceVariant
                )
            }
            Switch(
                checked = state.isBiometricLockEnabled,
                onCheckedChange = onBiometricToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = AscendPalette.Primary)
            )
        }
    }
}

package com.ascend75.feature.onboarding

import com.ascend75.core.common.domain.ChallengeMode

data class OnboardingUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 6,
    val isDisclaimerAccepted: Boolean = false,
    val fitnessLevel: String = "Intermediate",
    val primaryMotivation: String = "Mental Toughness & Circadian Alignment",
    val wakeHour: Int = 6,
    val wakeMinute: Int = 30,
    val sleepCutoffHour: Int = 3,
    val sleepCutoffMinute: Int = 0,
    val selectedMode: ChallengeMode = ChallengeMode.STRICT_75,
    val waterTargetMl: Int = 3800,
    val readingTargetPages: Int = 10,
    val isBiometricLockEnabled: Boolean = false,
    val isCompleting: Boolean = false,
    val errorMessage: String? = null
)

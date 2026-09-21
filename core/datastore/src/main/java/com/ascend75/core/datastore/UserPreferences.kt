package com.ascend75.core.datastore

data class UserPreferences(
    val isOnboardingCompleted: Boolean = false,
    val activeChallengeId: String? = null,
    val wakeHour: Int = 6,
    val wakeMinute: Int = 30,
    val sleepCutoffHour: Int = 3,
    val sleepCutoffMinute: Int = 0,
    val selectedMode: String = "STRICT_75",
    val isBiometricEnabled: Boolean = false,
    val quietHoursStartHour: Int = 22,
    val quietHoursEndHour: Int = 7,
    val lastCelebratedDay: Int = 0
)

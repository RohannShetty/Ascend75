package com.ascend75.core.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun verifyDefaultPreferencesValues() {
        val prefs = UserPreferences()

        assertFalse(prefs.isOnboardingCompleted)
        assertNull(prefs.activeChallengeId)
        assertEquals(3, prefs.sleepCutoffHour)
        assertEquals(0, prefs.sleepCutoffMinute)
        assertEquals("STRICT_75", prefs.selectedMode)
        assertFalse(prefs.isBiometricEnabled)
        assertEquals(22, prefs.quietHoursStartHour)
        assertEquals(7, prefs.quietHoursEndHour)
    }

    @Test
    fun verifyCustomPreferencesValues() {
        val prefs = UserPreferences(
            isOnboardingCompleted = true,
            activeChallengeId = "challenge-123",
            sleepCutoffHour = 4,
            sleepCutoffMinute = 30,
            selectedMode = "FLEXIBLE_75",
            isBiometricEnabled = true
        )

        assertEquals(true, prefs.isOnboardingCompleted)
        assertEquals("challenge-123", prefs.activeChallengeId)
        assertEquals(4, prefs.sleepCutoffHour)
        assertEquals(30, prefs.sleepCutoffMinute)
        assertEquals("FLEXIBLE_75", prefs.selectedMode)
        assertEquals(true, prefs.isBiometricEnabled)
    }
}

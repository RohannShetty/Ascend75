package com.ascend75.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ascend75.core.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ascend75_user_prefs")

@Singleton
class AscendPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ACTIVE_CHALLENGE_ID = stringPreferencesKey("active_challenge_id")
        val WAKE_HOUR = intPreferencesKey("wake_hour")
        val WAKE_MINUTE = intPreferencesKey("wake_minute")
        val SLEEP_CUTOFF_HOUR = intPreferencesKey("sleep_cutoff_hour")
        val SLEEP_CUTOFF_MINUTE = intPreferencesKey("sleep_cutoff_minute")
        val SELECTED_MODE = stringPreferencesKey("selected_mode")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val QUIET_START_HOUR = intPreferencesKey("quiet_start_hour")
        val QUIET_END_HOUR = intPreferencesKey("quiet_end_hour")
        val LAST_CELEBRATED_DAY = intPreferencesKey("last_celebrated_day")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            isOnboardingCompleted = prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            activeChallengeId = prefs[PreferencesKeys.ACTIVE_CHALLENGE_ID],
            wakeHour = prefs[PreferencesKeys.WAKE_HOUR] ?: 6,
            wakeMinute = prefs[PreferencesKeys.WAKE_MINUTE] ?: 30,
            sleepCutoffHour = prefs[PreferencesKeys.SLEEP_CUTOFF_HOUR] ?: 3,
            sleepCutoffMinute = prefs[PreferencesKeys.SLEEP_CUTOFF_MINUTE] ?: 0,
            selectedMode = prefs[PreferencesKeys.SELECTED_MODE] ?: "STRICT_75",
            isBiometricEnabled = prefs[PreferencesKeys.BIOMETRIC_ENABLED] ?: false,
            quietHoursStartHour = prefs[PreferencesKeys.QUIET_START_HOUR] ?: 22,
            quietHoursEndHour = prefs[PreferencesKeys.QUIET_END_HOUR] ?: 7,
            lastCelebratedDay = prefs[PreferencesKeys.LAST_CELEBRATED_DAY] ?: 0
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setActiveChallengeId(id: String?) {
        context.dataStore.edit { prefs ->
            if (id != null) {
                prefs[PreferencesKeys.ACTIVE_CHALLENGE_ID] = id
            } else {
                prefs.remove(PreferencesKeys.ACTIVE_CHALLENGE_ID)
            }
        }
    }

    suspend fun setSleepCutoff(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SLEEP_CUTOFF_HOUR] = hour
            prefs[PreferencesKeys.SLEEP_CUTOFF_MINUTE] = minute
        }
    }

    suspend fun setSelectedMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_MODE] = mode }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setLastCelebratedDay(day: Int) {
        context.dataStore.edit { it[PreferencesKeys.LAST_CELEBRATED_DAY] = day }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}

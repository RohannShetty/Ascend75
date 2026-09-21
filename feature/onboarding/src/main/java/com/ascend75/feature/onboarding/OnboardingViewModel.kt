package com.ascend75.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.common.domain.ChallengeMode
import com.ascend75.core.common.domain.ChallengeRulesEngine
import com.ascend75.core.common.domain.DayBoundaryEvaluator
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.entities.ChallengeInstanceEntity
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.datastore.AscendPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val dailyRecordDao: DailyRecordDao,
    private val taskEntryDao: TaskEntryDao,
    private val preferencesDataSource: AscendPreferencesDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setDisclaimerAccepted(accepted: Boolean) {
        _uiState.update { it.copy(isDisclaimerAccepted = accepted, errorMessage = null) }
    }

    fun setFitnessLevel(level: String) {
        _uiState.update { it.copy(fitnessLevel = level) }
    }

    fun setPrimaryMotivation(motivation: String) {
        _uiState.update { it.copy(primaryMotivation = motivation) }
    }

    fun setSleepCutoff(hour: Int, minute: Int) {
        _uiState.update { it.copy(sleepCutoffHour = hour, sleepCutoffMinute = minute) }
    }

    fun setSelectedMode(mode: ChallengeMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun setWaterTarget(ml: Int) {
        _uiState.update { it.copy(waterTargetMl = ml) }
    }

    fun setBiometricLock(enabled: Boolean) {
        _uiState.update { it.copy(isBiometricLockEnabled = enabled) }
    }

    fun nextStep() {
        _uiState.update { state ->
            if (state.currentStep == 1 && !state.isDisclaimerAccepted) {
                state.copy(errorMessage = "You must read and acknowledge the medical disclaimer to continue.")
            } else if (state.currentStep < state.totalSteps) {
                state.copy(currentStep = state.currentStep + 1, errorMessage = null)
            } else {
                state
            }
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            if (state.currentStep > 1) {
                state.copy(currentStep = state.currentStep - 1, errorMessage = null)
            } else {
                state
            }
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true, errorMessage = null) }
            try {
                val state = _uiState.value
                val challengeId = UUID.randomUUID().toString()
                val recordId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                val today = LocalDate.now()

                val cutoffTimestamp = DayBoundaryEvaluator.calculateSleepCutoffTimestamp(
                    calendarDate = today,
                    sleepCutoffTime = LocalTime.of(state.sleepCutoffHour, state.sleepCutoffMinute)
                )

                // 1. Create active challenge
                val challenge = ChallengeInstanceEntity(
                    id = challengeId,
                    attemptNumber = 1,
                    mode = state.selectedMode.name,
                    status = "ACTIVE",
                    startedAt = now,
                    configJson = JSONObject()
                        .put("fitnessLevel", state.fitnessLevel)
                        .put("motivation", state.primaryMotivation)
                        .toString()
                )
                challengeDao.insertChallenge(challenge)

                // 2. Create Day 1 record
                val dailyRecord = DailyRecordEntity(
                    id = recordId,
                    challengeInstanceId = challengeId,
                    dayNumber = 1,
                    calendarDate = today.toEpochDay(),
                    isCompleted = false,
                    sleepCutoffTimestamp = cutoffTimestamp
                )
                dailyRecordDao.insertDailyRecord(dailyRecord)

                // 3. Create initial tasks
                val taskSpecs = ChallengeRulesEngine.getTasksForMode(state.selectedMode)
                val taskEntities = taskSpecs.map { spec ->
                    TaskEntryEntity(
                        id = UUID.randomUUID().toString(),
                        dailyRecordId = recordId,
                        habitType = spec.habitType,
                        isCompleted = false,
                        targetValue = when (spec.habitType) {
                            // Only override the mode's water spec when the user actually moved the slider.
                            "WATER" -> if (state.waterTargetMl != DEFAULT_WATER_TARGET_ML) {
                                state.waterTargetMl.toDouble()
                            } else {
                                spec.targetValue
                            }
                            "READING" -> state.readingTargetPages.toDouble()
                            else -> spec.targetValue
                        }
                    )
                }
                taskEntryDao.insertTaskEntries(taskEntities)

                // 4. Update persistent preferences
                preferencesDataSource.setOnboardingCompleted(true)
                preferencesDataSource.setActiveChallengeId(challengeId)
                preferencesDataSource.setSelectedMode(state.selectedMode.name)
                preferencesDataSource.setSleepCutoff(state.sleepCutoffHour, state.sleepCutoffMinute)
                preferencesDataSource.setBiometricEnabled(state.isBiometricLockEnabled)

                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isCompleting = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    private companion object {
        const val DEFAULT_WATER_TARGET_ML = 3800
    }
}

package com.ascend75.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.domain.model.HabitType
import com.ascend75.core.domain.repository.ChallengeRepository
import com.ascend75.core.domain.repository.DailyRecordRepository
import com.ascend75.core.domain.repository.SettingsRepository
import com.ascend75.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val dailyRecordRepository: DailyRecordRepository,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository
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
                val configJson = JSONObject()
                    .put("fitnessLevel", state.fitnessLevel)
                    .put("motivation", state.primaryMotivation)
                    .toString()

                // One transactional entry point: challenge + Day 1 + the mode's habit set.
                val challengeId = challengeRepository.startAttempt(
                    mode = state.selectedMode.name,
                    configJson = configJson,
                    sleepCutoffHour = state.sleepCutoffHour,
                    sleepCutoffMinute = state.sleepCutoffMinute
                )

                // Honour the customiser sliders when the user actually moved them.
                val customWater = state.waterTargetMl.takeIf { it != DEFAULT_WATER_TARGET_ML }
                val dayOneTasks = dailyRecordRepository.tasksForDay(challengeId, dayNumber = 1)
                dayOneTasks.forEach { task ->
                    when {
                        task.habitType == HabitType.WATER && customWater != null ->
                            taskRepository.updateTask(task.copy(targetValue = customWater.toDouble()))

                        task.habitType == HabitType.READING ->
                            taskRepository.updateTask(task.copy(targetValue = state.readingTargetPages.toDouble()))
                    }
                }

                settingsRepository.setSleepCutoff(state.sleepCutoffHour, state.sleepCutoffMinute)
                settingsRepository.setBiometricEnabled(state.isBiometricLockEnabled)

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

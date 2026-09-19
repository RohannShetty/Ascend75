package com.ascend75.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.entities.TaskEntryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val isOutdoor: Boolean = true,
    val workoutType: String = "RUNNING",
    val intensity: String = "MODERATE",
    val remainingSeconds: Int = 45 * 60,
    val isTimerRunning: Boolean = false,
    val isSessionFinished: Boolean = false,
    val showEarlyFinishWarning: Boolean = false,
    val showSeparationWarning: Boolean = false,
    val lastWorkoutFinishTimestamp: Long? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val taskEntryDao: TaskEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    fun setOutdoor(isOutdoor: Boolean) {
        _uiState.update { it.copy(isOutdoor = isOutdoor) }
    }

    fun setWorkoutType(type: String) {
        _uiState.update { it.copy(workoutType = type) }
    }

    fun setIntensity(intensity: String) {
        _uiState.update { it.copy(intensity = intensity) }
    }

    fun updateTimer(remainingSeconds: Int, isRunning: Boolean) {
        _uiState.update { it.copy(remainingSeconds = remainingSeconds, isTimerRunning = isRunning) }
    }

    fun checkWorkoutSeparation(lastWorkoutFinishTime: Long?) {
        if (lastWorkoutFinishTime != null) {
            val threeHoursMs = 3 * 60 * 60 * 1000L
            val timeSince = System.currentTimeMillis() - lastWorkoutFinishTime
            if (timeSince < threeHoursMs) {
                _uiState.update { it.copy(showSeparationWarning = true) }
            }
        }
    }

    fun dismissSeparationWarning() {
        _uiState.update { it.copy(showSeparationWarning = false) }
    }

    fun completeWorkout(taskId: String, onFinished: () -> Unit) {
        val elapsed = (45 * 60) - _uiState.value.remainingSeconds
        if (elapsed < 45 * 60) {
            _uiState.update { it.copy(showEarlyFinishWarning = true) }
            return
        }

        saveWorkoutSession(taskId, onFinished)
    }

    fun forceFinishWorkout(taskId: String, onFinished: () -> Unit) {
        _uiState.update { it.copy(showEarlyFinishWarning = false) }
        saveWorkoutSession(taskId, onFinished)
    }

    fun dismissEarlyWarning() {
        _uiState.update { it.copy(showEarlyFinishWarning = false) }
    }

    private fun saveWorkoutSession(taskId: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            taskEntryDao.updateTaskCompletion(taskId, isCompleted = true, completedAt = System.currentTimeMillis())
            _uiState.update { it.copy(isSessionFinished = true) }
            onFinished()
        }
    }
}

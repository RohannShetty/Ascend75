package com.ascend75.feature.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.database.dao.TaskEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WaterLogEntry(
    val amountMl: Int,
    val timestamp: Long
)

data class WaterUiState(
    val targetMl: Int = 3800,
    val currentTotalMl: Int = 0,
    val logs: List<WaterLogEntry> = emptyList(),
    val showHyponatremiaWarning: Boolean = false,
    val isCompleted: Boolean = false
)

@HiltViewModel
class WaterViewModel @Inject constructor(
    private val taskEntryDao: TaskEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterUiState())
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    fun initialize(targetMl: Int, currentMl: Int) {
        _uiState.update {
            it.copy(
                targetMl = targetMl,
                currentTotalMl = currentMl,
                isCompleted = currentMl >= targetMl
            )
        }
    }

    /**
     * Checks if logging `amountMl` exceeds the 1200ml / 60-minute hyponatremia threshold.
     */
    fun checkHyponatremiaRisk(amountMl: Int, now: Long = System.currentTimeMillis()): Boolean {
        val oneHourAgo = now - 60 * 60 * 1000L
        val recentSum = _uiState.value.logs
            .filter { it.timestamp >= oneHourAgo }
            .sumOf { it.amountMl }

        return (recentSum + amountMl) > 1200
    }

    fun logWater(amountMl: Int, taskId: String, forceLog: Boolean = false) {
        val now = System.currentTimeMillis()
        val risk = checkHyponatremiaRisk(amountMl, now)

        if (risk && !forceLog) {
            _uiState.update { it.copy(showHyponatremiaWarning = true) }
            return
        }

        val newTotal = _uiState.value.currentTotalMl + amountMl
        val isDone = newTotal >= _uiState.value.targetMl

        viewModelScope.launch {
            taskEntryDao.updateTaskCurrentValue(taskId, newTotal.toDouble())
            if (isDone) {
                taskEntryDao.updateTaskCompletion(taskId, true, now)
            }

            _uiState.update { state ->
                state.copy(
                    currentTotalMl = newTotal,
                    logs = state.logs + WaterLogEntry(amountMl, now),
                    isCompleted = isDone,
                    showHyponatremiaWarning = false
                )
            }
        }
    }

    fun dismissWarning() {
        _uiState.update { it.copy(showHyponatremiaWarning = false) }
    }
}

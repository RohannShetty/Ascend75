package com.ascend75.feature.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.dao.WaterLogDao
import com.ascend75.core.database.entities.WaterLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
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
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WaterViewModel @Inject constructor(
    private val taskEntryDao: TaskEntryDao,
    private val waterLogDao: WaterLogDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterUiState())
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    /** Amount that triggered the rate warning, replayed if the user chooses "Log Anyway". */
    private var pendingAmountMl: Int? = null

    /**
     * Restores the running total and the rolling hour window from persisted logs, so a redraw or a
     * process restart does not reset the pacing clock.
     */
    suspend fun initializeFrom(taskId: String) {
        val task = taskEntryDao.getTaskById(taskId)
        if (task == null) {
            _uiState.update { it.copy(errorMessage = "Hydration task not found.") }
            return
        }

        val storedLogs = waterLogDao.getSince(taskId, 0L)
        val restoredTotal = storedLogs.sumOf { it.amountMl }
        val targetMl = task.targetValue.toInt()

        _uiState.update {
            it.copy(
                targetMl = targetMl,
                currentTotalMl = restoredTotal,
                logs = storedLogs.map { log -> WaterLogEntry(log.amountMl, log.loggedAt) },
                isCompleted = task.isCompleted,
                errorMessage = null
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
            pendingAmountMl = amountMl
            _uiState.update { it.copy(showHyponatremiaWarning = true) }
            return
        }

        pendingAmountMl = null
        viewModelScope.launch {
            waterLogDao.insert(
                WaterLogEntity(
                    id = UUID.randomUUID().toString(),
                    taskEntryId = taskId,
                    amountMl = amountMl,
                    loggedAt = now
                )
            )

            val newTotal = _uiState.value.currentTotalMl + amountMl
            val isDone = newTotal >= _uiState.value.targetMl

            taskEntryDao.updateTaskCurrentValue(taskId, newTotal.toDouble())
            if (isDone) {
                taskEntryDao.updateTaskCompletion(taskId, isCompleted = true, completedAt = now)
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

    /** Records the intake that raised the rate warning. */
    fun confirmPendingLog(taskId: String) {
        val amount = pendingAmountMl ?: return
        logWater(amount, taskId, forceLog = true)
    }

    fun dismissWarning() {
        pendingAmountMl = null
        _uiState.update { it.copy(showHyponatremiaWarning = false) }
    }
}

package com.ascend75.feature.dashboard

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val dailyRecordDao: DailyRecordDao,
    private val taskEntryDao: TaskEntryDao,
    private val preferencesDataSource: AscendPreferencesDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(
                challengeDao.observeActiveChallenge(),
                preferencesDataSource.userPreferencesFlow
            ) { challenge, prefs ->
                Pair(challenge, prefs)
            }.collect { (challenge, prefs) ->
                if (challenge != null) {
                    val latestDayNumber = dailyRecordDao.getLatestDayNumber(challenge.id) ?: 1
                    dailyRecordDao.observeDailyRecordWithTasks(challenge.id, latestDayNumber)
                        .collect { recordWithTasks ->
                            if (recordWithTasks != null) {
                                val tasks = recordWithTasks.tasks
                                val completed = tasks.count { it.isCompleted }
                                val mode = ChallengeMode.fromString(challenge.mode)
                                val isPastCutoff = DayBoundaryEvaluator.isPastSleepCutoff(
                                    currentTimestamp = System.currentTimeMillis(),
                                    sleepCutoffTimestamp = recordWithTasks.dailyRecord.sleepCutoffTimestamp
                                )

                                val showReset = isPastCutoff && completed < tasks.size && mode == ChallengeMode.STRICT_75

                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        dayNumber = recordWithTasks.dailyRecord.dayNumber,
                                        mode = mode,
                                        streakDays = recordWithTasks.dailyRecord.dayNumber,
                                        tasks = tasks,
                                        completedCount = completed,
                                        totalCount = tasks.size,
                                        isPastCutoff = isPastCutoff,
                                        showResetDialog = showReset
                                    )
                                }
                            }
                        }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val completedAt = if (isCompleted) System.currentTimeMillis() else null
            taskEntryDao.updateTaskCompletion(taskId, isCompleted, completedAt)
        }
    }

    fun selectTaskForDetail(task: TaskEntryEntity?) {
        _uiState.update { it.copy(selectedTaskForDetail = task) }
    }

    fun saveTaskNotes(taskId: String, note: String) {
        viewModelScope.launch {
            val existing = taskEntryDao.getTaskById(taskId)
            if (existing != null) {
                taskEntryDao.updateTaskEntry(existing.copy(notes = note))
                _uiState.update { state ->
                    state.copy(
                        selectedTaskForDetail = state.selectedTaskForDetail?.copy(notes = note)
                    )
                }
            }
        }
    }

    fun archiveAndResetStrictAttempt(reflectionNote: String, switchToFlexible: Boolean) {
        viewModelScope.launch {
            val challenge = challengeDao.getActiveChallenge() ?: return@launch

            // 1. Archive previous attempt
            challengeDao.updateChallenge(
                challenge.copy(
                    status = "RESET_ARCHIVED",
                    endedAt = System.currentTimeMillis()
                )
            )

            // 2. Start new attempt #N+1
            val newChallengeId = UUID.randomUUID().toString()
            val newMode = if (switchToFlexible) ChallengeMode.FLEXIBLE_75 else ChallengeMode.STRICT_75
            val newChallenge = ChallengeInstanceEntity(
                id = newChallengeId,
                attemptNumber = challenge.attemptNumber + 1,
                mode = newMode.name,
                status = "ACTIVE",
                startedAt = System.currentTimeMillis(),
                configJson = challenge.configJson
            )
            challengeDao.insertChallenge(newChallenge)

            // 3. Create Day 1 for new attempt
            val today = LocalDate.now()
            val cutoffTimestamp = DayBoundaryEvaluator.calculateSleepCutoffTimestamp(
                calendarDate = today,
                sleepCutoffTime = LocalTime.of(3, 0)
            )
            val recordId = UUID.randomUUID().toString()
            val newDailyRecord = DailyRecordEntity(
                id = recordId,
                challengeInstanceId = newChallengeId,
                dayNumber = 1,
                calendarDate = today.toEpochDay(),
                isCompleted = false,
                sleepCutoffTimestamp = cutoffTimestamp,
                reflectionNotes = reflectionNote
            )
            dailyRecordDao.insertDailyRecord(newDailyRecord)

            // 4. Generate initial tasks
            val taskSpecs = ChallengeRulesEngine.getTasksForMode(newMode)
            val taskEntities = taskSpecs.map { spec ->
                TaskEntryEntity(
                    id = UUID.randomUUID().toString(),
                    dailyRecordId = recordId,
                    habitType = spec.habitType,
                    isCompleted = false,
                    targetValue = spec.targetValue
                )
            }
            taskEntryDao.insertTaskEntries(taskEntities)

            // 5. Update preferences
            preferencesDataSource.setActiveChallengeId(newChallengeId)
            preferencesDataSource.setSelectedMode(newMode.name)

            _uiState.update { it.copy(showResetDialog = false) }
        }
    }
}

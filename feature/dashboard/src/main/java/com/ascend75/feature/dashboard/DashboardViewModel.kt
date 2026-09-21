package com.ascend75.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.common.domain.ChallengeMode
import com.ascend75.core.common.domain.ChallengeRulesEngine
import com.ascend75.core.common.domain.DayBoundaryEvaluator
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.database.dao.DailyRecordDao
import com.ascend75.core.database.dao.ScienceCardDao
import com.ascend75.core.database.dao.TaskEntryDao
import com.ascend75.core.database.entities.ChallengeInstanceEntity
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.ScienceCardEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.datastore.AscendPreferencesDataSource
import com.ascend75.feature.dashboard.data.DailyProtocolRepository
import com.ascend75.feature.dashboard.data.TodayProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

private val MilestoneDays = setOf(1, 7, 14, 21, 30, 45, 60, 75)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val dailyRecordDao: DailyRecordDao,
    private val taskEntryDao: TaskEntryDao,
    private val scienceCardDao: ScienceCardDao,
    private val preferencesDataSource: AscendPreferencesDataSource,
    private val dailyProtocolRepository: DailyProtocolRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /** Day whose milestone dialog already closed, so a persisted-preference round trip cannot re-open it. */
    private var dismissedMilestoneDay: Int? = null

    /** Suppresses the "no active challenge" error during the archive/restart window of a Strict reset. */
    private var isResetting = false

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            dailyProtocolRepository.observeToday()
                .flatMapLatest { protocol ->
                    if (protocol == null) {
                        flowOf<Pair<TodayProtocol?, ScienceCardEntity?>>(null to null)
                    } else {
                        scienceCardDao.observeCardByDay(protocol.record.dayNumber)
                            .map { card -> protocol to card }
                    }
                }
                .collect { (protocol, card) ->
                    if (protocol == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = if (isResetting) null else "No active challenge found."
                            )
                        }
                    } else {
                        applyProtocol(protocol, card)
                        if (protocol.isPastCutoff) {
                            dailyProtocolRepository.advanceDayIfDue(protocol)
                        }
                    }
                }
        }
    }

    private fun applyProtocol(protocol: TodayProtocol, card: ScienceCardEntity?) {
        val completed = protocol.tasks.count { it.isCompleted }
        val isStrictFailure = protocol.isPastCutoff &&
            completed < protocol.tasks.size &&
            protocol.mode == ChallengeMode.STRICT_75

        _uiState.update { state ->
            val shouldCelebrate = protocol.record.dayNumber in MilestoneDays &&
                protocol.prefs.lastCelebratedDay < protocol.record.dayNumber &&
                dismissedMilestoneDay != protocol.record.dayNumber &&
                !state.showMilestoneDialog

            state.copy(
                isLoading = false,
                dayNumber = protocol.record.dayNumber,
                mode = protocol.mode,
                streakDays = protocol.streakDays,
                tasks = protocol.tasks,
                completedCount = completed,
                totalCount = protocol.tasks.size,
                isPastCutoff = protocol.isPastCutoff,
                showResetDialog = isStrictFailure,
                showMilestoneDialog = state.showMilestoneDialog || shouldCelebrate,
                dailyScienceCardTitle = card?.title,
                sleepCutoffHour = protocol.prefs.sleepCutoffHour,
                sleepCutoffMinute = protocol.prefs.sleepCutoffMinute,
                errorMessage = null
            )
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

    fun dismissMilestone() {
        val day = _uiState.value.dayNumber
        dismissedMilestoneDay = day
        _uiState.update { it.copy(showMilestoneDialog = false) }
        viewModelScope.launch { preferencesDataSource.setLastCelebratedDay(day) }
    }

    fun archiveAndResetStrictAttempt(reflectionNote: String, switchToFlexible: Boolean) {
        viewModelScope.launch {
            isResetting = true
            try {
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

                // 3. Create Day 1 for new attempt using the user's configured cutoff
                val prefs = _uiState.value
                val today = LocalDate.now()
                val cutoffTimestamp = DayBoundaryEvaluator.calculateSleepCutoffTimestamp(
                    calendarDate = today,
                    sleepCutoffTime = LocalTime.of(prefs.sleepCutoffHour, prefs.sleepCutoffMinute)
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
                val taskEntities = ChallengeRulesEngine.getTasksForMode(newMode).map { spec ->
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
            } finally {
                isResetting = false
            }
        }
    }
}

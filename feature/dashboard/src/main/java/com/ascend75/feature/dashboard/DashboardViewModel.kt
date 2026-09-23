package com.ascend75.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.domain.model.TaskEntry
import com.ascend75.core.domain.model.TodayProtocol
import com.ascend75.core.domain.repository.ChallengeRepository
import com.ascend75.core.domain.repository.ScienceRepository
import com.ascend75.core.domain.repository.SettingsRepository
import com.ascend75.core.domain.repository.TaskRepository
import com.ascend75.core.domain.repository.TodayProtocolRepository
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
import javax.inject.Inject

private val MilestoneDays = setOf(1, 7, 14, 21, 30, 45, 60, 75)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val scienceRepository: ScienceRepository,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val todayProtocolRepository: TodayProtocolRepository
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
            todayProtocolRepository.observeToday()
                .flatMapLatest { protocol ->
                    if (protocol == null) {
                        flowOf(null to null)
                    } else {
                        scienceRepository.observeCardByDay(protocol.record.dayNumber)
                            .map { card -> protocol to card?.title }
                    }
                }
                .collect { (protocol, cardTitle) ->
                    if (protocol == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = if (isResetting) null else "No active challenge found."
                            )
                        }
                    } else {
                        applyProtocol(protocol, cardTitle)
                        if (protocol.isPastCutoff) {
                            todayProtocolRepository.advanceDayIfDue(protocol)
                        }
                    }
                }
        }
    }

    private fun applyProtocol(protocol: TodayProtocol, cardTitle: String?) {
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
                dailyScienceCardTitle = cardTitle,
                sleepCutoffHour = protocol.prefs.sleepCutoffHour,
                sleepCutoffMinute = protocol.prefs.sleepCutoffMinute,
                errorMessage = null
            )
        }
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val completedAt = if (isCompleted) System.currentTimeMillis() else null
            taskRepository.setCompletion(taskId, isCompleted, completedAt)
        }
    }

    fun selectTaskForDetail(task: TaskEntry?) {
        _uiState.update { it.copy(selectedTaskForDetail = task) }
    }

    fun saveTaskNotes(taskId: String, note: String) {
        viewModelScope.launch {
            taskRepository.saveNotes(taskId, note)
            _uiState.update { state ->
                state.copy(
                    selectedTaskForDetail = state.selectedTaskForDetail?.copy(notes = note)
                )
            }
        }
    }

    fun dismissMilestone() {
        val day = _uiState.value.dayNumber
        dismissedMilestoneDay = day
        _uiState.update { it.copy(showMilestoneDialog = false) }
        viewModelScope.launch { settingsRepository.setLastCelebratedDay(day) }
    }

    /**
     * Archives the failed Strict attempt and starts a fresh one. All database writes happen inside
     * the challenge repository's transaction, so a process death cannot leave a half-reset state.
     */
    fun archiveAndResetStrictAttempt(reflectionNote: String, switchToFlexible: Boolean) {
        viewModelScope.launch {
            isResetting = true
            try {
                challengeRepository.archiveAndRestart(
                    reflectionNote = reflectionNote,
                    newMode = if (switchToFlexible) ChallengeMode.FLEXIBLE_75.name else ChallengeMode.STRICT_75.name,
                    sleepCutoffHour = _uiState.value.sleepCutoffHour,
                    sleepCutoffMinute = _uiState.value.sleepCutoffMinute
                )
                _uiState.update { it.copy(showResetDialog = false) }
            } finally {
                isResetting = false
            }
        }
    }
}

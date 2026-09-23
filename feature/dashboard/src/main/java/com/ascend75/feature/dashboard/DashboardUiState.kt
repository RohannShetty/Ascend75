package com.ascend75.feature.dashboard

import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.domain.model.TaskEntry

data class DashboardUiState(
    val isLoading: Boolean = true,
    val dayNumber: Int = 1,
    val mode: ChallengeMode = ChallengeMode.STRICT_75,
    val streakDays: Int = 0,
    val tasks: List<TaskEntry> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 6,
    val isPastCutoff: Boolean = false,
    val showResetDialog: Boolean = false,
    val showMilestoneDialog: Boolean = false,
    val selectedTaskForDetail: TaskEntry? = null,
    val dailyScienceCardTitle: String? = null,
    val sleepCutoffHour: Int = 3,
    val sleepCutoffMinute: Int = 0,
    val errorMessage: String? = null
)

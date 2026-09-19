package com.ascend75.feature.dashboard

import com.ascend75.core.common.domain.ChallengeMode
import com.ascend75.core.database.entities.TaskEntryEntity

data class DashboardUiState(
    val isLoading: Boolean = true,
    val dayNumber: Int = 1,
    val mode: ChallengeMode = ChallengeMode.STRICT_75,
    val streakDays: Int = 1,
    val tasks: List<TaskEntryEntity> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 6,
    val isPastCutoff: Boolean = false,
    val showResetDialog: Boolean = false,
    val selectedTaskForDetail: TaskEntryEntity? = null,
    val dailyScienceCardTitle: String? = "Morning Sunlight & Adenosine Clearance",
    val errorMessage: String? = null
)

package com.ascend75.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.domain.model.HabitType
import com.ascend75.core.domain.model.TaskEntry
import com.ascend75.core.domain.repository.TodayProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackersHubUiState(
    val isLoading: Boolean = true,
    val dayNumber: Int = 1,
    val workouts: List<TaskEntry> = emptyList(),
    val water: TaskEntry? = null,
    val reading: TaskEntry? = null,
    val photo: TaskEntry? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TrackersHubViewModel @Inject constructor(
    private val todayProtocolRepository: TodayProtocolRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackersHubUiState())
    val uiState: StateFlow<TrackersHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            todayProtocolRepository.observeToday().collect { protocol ->
                if (protocol == null) {
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    val tasks = protocol.tasks
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dayNumber = protocol.record.dayNumber,
                            workouts = tasks
                                .filter { task -> task.habitType.isWorkout }
                                .sortedBy { task -> task.habitType.ordinal },
                            water = tasks.firstOrNull { task -> task.habitType == HabitType.WATER },
                            reading = tasks.firstOrNull { task -> task.habitType == HabitType.READING },
                            photo = tasks.firstOrNull { task -> task.habitType == HabitType.PHOTO },
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
}

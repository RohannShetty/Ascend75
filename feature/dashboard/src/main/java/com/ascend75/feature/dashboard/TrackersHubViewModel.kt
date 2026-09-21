package com.ascend75.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.feature.dashboard.data.DailyProtocolRepository
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
    val workouts: List<TaskEntryEntity> = emptyList(),
    val water: TaskEntryEntity? = null,
    val reading: TaskEntryEntity? = null,
    val photo: TaskEntryEntity? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TrackersHubViewModel @Inject constructor(
    private val dailyProtocolRepository: DailyProtocolRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackersHubUiState())
    val uiState: StateFlow<TrackersHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dailyProtocolRepository.observeToday().collect { protocol ->
                if (protocol == null) {
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    val tasks = protocol.tasks
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dayNumber = protocol.record.dayNumber,
                            workouts = tasks
                                .filter { task -> task.habitType.startsWith("WORKOUT") }
                                .sortedBy { task -> task.habitType },
                            water = tasks.firstOrNull { task -> task.habitType == "WATER" },
                            reading = tasks.firstOrNull { task -> task.habitType == "READING" },
                            photo = tasks.firstOrNull { task -> task.habitType == "PHOTO" },
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
}

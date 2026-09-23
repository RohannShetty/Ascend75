package com.ascend75.feature.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.domain.model.ReadingSession
import com.ascend75.core.domain.repository.ReadingRepository
import com.ascend75.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ReadingUiState(
    val bookTitle: String = "",
    val startPage: Int = 1,
    val endPage: Int = 1,
    val pagesRead: Int = 0,
    val readingDurationSeconds: Int = 0,
    val keyTakeaway: String = "",
    val isCompleted: Boolean = false,
    val isTimerRunning: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val readingRepository: ReadingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingUiState())
    val uiState: StateFlow<ReadingUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun setBookTitle(title: String) {
        _uiState.update { it.copy(bookTitle = title) }
    }

    fun setPages(start: Int, end: Int) {
        val count = (end - start).coerceAtLeast(0)
        _uiState.update {
            it.copy(
                startPage = start,
                endPage = end,
                pagesRead = count,
                isCompleted = count >= 10
            )
        }
    }

    fun setTakeaway(takeaway: String) {
        _uiState.update { it.copy(keyTakeaway = takeaway) }
    }

    fun startTimer() {
        if (timerJob?.isActive == true) return
        _uiState.update { it.copy(isTimerRunning = true) }
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _uiState.update { it.copy(readingDurationSeconds = it.readingDurationSeconds + 1) }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun saveSession(taskId: String, onFinished: () -> Unit) {
        val state = _uiState.value
        if (state.pagesRead < 10) {
            _uiState.update { it.copy(errorMessage = "Ascend 75 requires reading at least 10 pages.") }
            return
        }

        stopTimer()
        val durationSeconds = _uiState.value.readingDurationSeconds
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            taskRepository.setCurrentValue(taskId, state.pagesRead.toDouble())
            taskRepository.setCompletion(taskId, isCompleted = true, completedAt = now)
            taskRepository.saveNotes(
                taskId,
                "Book: ${state.bookTitle}\nPages: ${state.startPage}-${state.endPage}\nTakeaway: ${state.keyTakeaway}"
            )

            readingRepository.addSession(
                ReadingSession(
                    id = UUID.randomUUID().toString(),
                    taskEntryId = taskId,
                    bookTitle = state.bookTitle.ifBlank { "Untitled" },
                    startPage = state.startPage,
                    endPage = state.endPage,
                    pagesRead = state.pagesRead,
                    readingDurationSeconds = durationSeconds,
                    keyTakeaway = state.keyTakeaway.ifBlank { null },
                    loggedAt = now
                )
            )

            onFinished()
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}

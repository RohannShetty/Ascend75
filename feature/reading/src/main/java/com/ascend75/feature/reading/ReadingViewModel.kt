package com.ascend75.feature.reading

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

data class ReadingUiState(
    val bookTitle: String = "",
    val startPage: Int = 1,
    val endPage: Int = 11,
    val pagesRead: Int = 10,
    val readingDurationMinutes: Int = 20,
    val keyTakeaway: String = "",
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val taskEntryDao: TaskEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingUiState())
    val uiState: StateFlow<ReadingUiState> = _uiState.asStateFlow()

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

    fun saveSession(taskId: String, onFinished: () -> Unit) {
        val state = _uiState.value
        if (state.pagesRead < 10) {
            _uiState.update { it.copy(errorMessage = "Ascend 75 requires reading at least 10 pages.") }
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            taskEntryDao.updateTaskCurrentValue(taskId, state.pagesRead.toDouble())
            taskEntryDao.updateTaskCompletion(taskId, isCompleted = true, completedAt = now)

            val existing = taskEntryDao.getTaskById(taskId)
            if (existing != null) {
                val notes = "Book: ${state.bookTitle}\nPages: ${state.startPage}-${state.endPage}\nTakeaway: ${state.keyTakeaway}"
                taskEntryDao.updateTaskEntry(existing.copy(notes = notes))
            }

            onFinished()
        }
    }
}

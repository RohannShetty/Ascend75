package com.ascend75.feature.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.crypto.VaultFileStorage
import com.ascend75.core.database.dao.TaskEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class VaultPhotoItem(
    val dayNumber: Int,
    val filePath: String,
    val timestamp: Long
)

data class PhotoVaultUiState(
    val isLocked: Boolean = true,
    val photos: List<VaultPhotoItem> = emptyList(),
    val splitSliderPosition: Float = 0.5f,
    val isCapturing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PhotoVaultViewModel @Inject constructor(
    private val vaultFileStorage: VaultFileStorage,
    private val taskEntryDao: TaskEntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoVaultUiState())
    val uiState: StateFlow<PhotoVaultUiState> = _uiState.asStateFlow()

    fun unlockVault() {
        _uiState.update { it.copy(isLocked = false) }
    }

    fun lockVault() {
        _uiState.update { it.copy(isLocked = true) }
    }

    fun setSplitSliderPosition(position: Float) {
        _uiState.update { it.copy(splitSliderPosition = position.coerceIn(0f, 1f)) }
    }

    fun saveCapturedPhoto(taskId: String, dayNumber: Int, imageBytes: ByteArray, onFinished: () -> Unit) {
        viewModelScope.launch {
            try {
                val filename = "photo_day_${dayNumber}_${UUID.randomUUID()}"
                val encryptedFile = vaultFileStorage.saveEncryptedPhoto(filename, imageBytes)

                val now = System.currentTimeMillis()
                taskEntryDao.updateTaskCompletion(taskId, isCompleted = true, completedAt = now)

                val existing = taskEntryDao.getTaskById(taskId)
                if (existing != null) {
                    taskEntryDao.updateTaskEntry(existing.copy(notes = encryptedFile.absolutePath))
                }

                _uiState.update { state ->
                    state.copy(
                        photos = state.photos + VaultPhotoItem(dayNumber, encryptedFile.absolutePath, now)
                    )
                }

                onFinished()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage) }
            }
        }
    }
}

package com.ascend75.feature.photos

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.crypto.BiometricAuthHelper
import com.ascend75.core.crypto.VaultFileStorage
import com.ascend75.core.domain.model.HabitType
import com.ascend75.core.domain.repository.ChallengeRepository
import com.ascend75.core.domain.repository.DailyRecordRepository
import com.ascend75.core.domain.repository.SettingsRepository
import com.ascend75.core.domain.repository.TaskRepository
import com.ascend75.core.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class VaultPhotoItem(
    val dayNumber: Int,
    val filePath: String,
    val timestamp: Long
)

data class PhotoVaultUiState(
    val isLocked: Boolean = true,
    val dayNumber: Int = 1,
    val taskId: String? = null,
    val photos: List<VaultPhotoItem> = emptyList(),
    val splitSliderPosition: Float = 0.5f,
    val isCapturing: Boolean = false,
    val captureCompleted: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PhotoVaultViewModel @Inject constructor(
    private val vaultFileStorage: VaultFileStorage,
    private val biometricAuthHelper: BiometricAuthHelper,
    private val taskRepository: TaskRepository,
    private val vaultRepository: VaultRepository,
    private val challengeRepository: ChallengeRepository,
    private val dailyRecordRepository: DailyRecordRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoVaultUiState())
    val uiState: StateFlow<PhotoVaultUiState> = _uiState.asStateFlow()

    init {
        observeVaultPhotos()
        observeCurrentDay()
        observeBiometricPreference()
    }

    private fun observeVaultPhotos() {
        viewModelScope.launch {
            vaultRepository.observeVaultPhotos().collect { photos ->
                _uiState.update { state ->
                    state.copy(
                        photos = photos.map { photo ->
                            VaultPhotoItem(
                                dayNumber = photo.dayNumber,
                                filePath = photo.encryptedFilePath,
                                timestamp = photo.capturedAt
                            )
                        }
                    )
                }
            }
        }
    }

    /** Resolves which day and which PHOTO task the vault is currently capturing for. */
    private fun observeCurrentDay() {
        viewModelScope.launch {
            challengeRepository.observeActiveChallenge()
                .distinctUntilChanged()
                .flatMapLatest { challenge ->
                    if (challenge == null) {
                        flowOf(null)
                    } else {
                        dailyRecordRepository.observeRecordsForChallenge(challenge.id)
                    }
                }
                .collect { records ->
                    val latest = records?.maxByOrNull { it.dayNumber }
                    if (latest == null) {
                        _uiState.update { it.copy(dayNumber = 1, taskId = null) }
                    } else {
                        val tasks = dailyRecordRepository.tasksForDay(
                            latest.challengeInstanceId,
                            latest.dayNumber
                        )
                        val photoTaskId = tasks.firstOrNull { it.habitType == HabitType.PHOTO }?.id
                        _uiState.update { it.copy(dayNumber = latest.dayNumber, taskId = photoTaskId) }
                    }
                }
        }
    }

    private fun observeBiometricPreference() {
        viewModelScope.launch {
            settingsRepository.preferences.collect { prefs ->
                _uiState.update { it.copy(isBiometricEnabled = prefs.isBiometricEnabled) }
            }
        }
    }

    /** Raises the system prompt when the gate is enabled, otherwise unlocks directly. */
    fun requestUnlock(activity: FragmentActivity) {
        val requiresAuth = _uiState.value.isBiometricEnabled && biometricAuthHelper.isBiometricOrPinAvailable()
        if (!requiresAuth) {
            unlockVault()
            return
        }

        biometricAuthHelper.promptBiometricAuth(
            activity = activity,
            onSuccess = { unlockVault() },
            onError = { message -> onAuthError(message) }
        )
    }

    fun unlockVault() {
        _uiState.update { it.copy(isLocked = false, errorMessage = null) }
    }

    fun lockVault() {
        _uiState.update { it.copy(isLocked = true, captureCompleted = false) }
    }

    fun onAuthError(message: String) {
        _uiState.update { it.copy(isLocked = true, errorMessage = message) }
    }

    fun setSplitSliderPosition(position: Float) {
        _uiState.update { it.copy(splitSliderPosition = position.coerceIn(0f, 1f)) }
    }

    fun startCapture() {
        _uiState.update { it.copy(isCapturing = true, captureCompleted = false, errorMessage = null) }
    }

    fun cancelCapture() {
        _uiState.update { it.copy(isCapturing = false) }
    }

    fun onCaptureError(message: String) {
        _uiState.update { it.copy(isCapturing = false, errorMessage = message) }
    }

    fun acknowledgeCapture() {
        _uiState.update { it.copy(captureCompleted = false) }
    }

    fun saveCapturedPhoto(taskId: String, dayNumber: Int, imageBytes: ByteArray, onFinished: () -> Unit) {
        viewModelScope.launch {
            try {
                val filename = "photo_day_${dayNumber}_${UUID.randomUUID()}"
                val photoHash = sha256Hex(imageBytes)
                val encryptedFile = vaultFileStorage.saveEncryptedPhoto(filename, imageBytes)
                // Plaintext is no longer needed once the ciphertext is on disk.
                imageBytes.fill(0)

                val now = System.currentTimeMillis()
                taskRepository.setCompletion(taskId, isCompleted = true, completedAt = now)
                vaultRepository.addPhoto(
                    id = UUID.randomUUID().toString(),
                    taskEntryId = taskId,
                    encryptedFilePath = encryptedFile.absolutePath,
                    photoHash = photoHash,
                    fileSizeBytes = encryptedFile.length(),
                    capturedAt = now
                )

                _uiState.update { it.copy(isCapturing = false, captureCompleted = true, errorMessage = null) }
                onFinished()
            } catch (e: Exception) {
                _uiState.update { it.copy(isCapturing = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    /** Decrypts a vault file for on-screen rendering. Returns null when the file cannot be read. */
    suspend fun readPhotoBytes(filePath: String): ByteArray? =
        runCatching { vaultFileStorage.readDecryptedPhoto(File(filePath)) }.getOrNull()

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString(separator = "") { byte -> String.format(Locale.US, "%02x", byte) }
}

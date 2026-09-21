package com.ascend75.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.common.domain.ChallengeMode
import com.ascend75.core.crypto.KeystoreManager
import com.ascend75.core.crypto.VaultFileStorage
import com.ascend75.core.database.dao.ChallengeDao
import com.ascend75.core.datastore.AscendPreferencesDataSource
import com.ascend75.core.datastore.UserPreferences
import com.ascend75.feature.settings.export.DataExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val appVersion: String = "",
    val isExporting: Boolean = false,
    val exportedFile: File? = null,
    val showWipeConfirmation: Boolean = false,
    val isWiping: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataSource: AscendPreferencesDataSource,
    private val dataExportManager: DataExportManager,
    private val vaultFileStorage: VaultFileStorage,
    private val keystoreManager: KeystoreManager,
    private val challengeDao: ChallengeDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesDataSource.userPreferencesFlow.collect { prefs ->
                _uiState.update { it.copy(userPreferences = prefs) }
            }
        }
        viewModelScope.launch { readAppVersion() }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.setBiometricEnabled(enabled)
        }
    }

    fun setSleepCutoff(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesDataSource.setSleepCutoff(hour, minute)
        }
    }

    /**
     * Writes the mode to preferences *and* to the active challenge row so the dashboard's mode pill
     * reflects the change. Today's habit rows are not re-materialised — that happens at the next
     * day boundary.
     */
    fun setMode(mode: ChallengeMode) {
        viewModelScope.launch {
            preferencesDataSource.setSelectedMode(mode.name)
            challengeDao.getActiveChallenge()?.let { active ->
                challengeDao.updateChallenge(active.copy(mode = mode.name))
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            val result = runCatching { dataExportManager.exportToFile() }
            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportedFile = result.getOrNull(),
                    errorMessage = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun promptDataWipe() {
        _uiState.update { it.copy(showWipeConfirmation = true) }
    }

    fun dismissWipeConfirmation() {
        _uiState.update { it.copy(showWipeConfirmation = false) }
    }

    fun executeDataWipe(onWiped: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWiping = true, showWipeConfirmation = false) }
            dataExportManager.executeCompleteDataWipe(
                onWipeVault = { vaultFileStorage.wipeAllVaultFiles() }
            )
            withContext(Dispatchers.IO) { keystoreManager.deleteMasterKey() }
            _uiState.update { it.copy(isWiping = false) }
            onWiped()
        }
    }

    private suspend fun readAppVersion() {
        val version = withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
            }.getOrDefault("")
        }
        _uiState.update { it.copy(appVersion = version) }
    }
}

package com.ascend75.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascend75.core.common.export.DataExportManager
import com.ascend75.core.crypto.VaultFileStorage
import com.ascend75.core.datastore.AscendPreferencesDataSource
import com.ascend75.core.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val isExporting: Boolean = false,
    val exportedJson: String? = null,
    val showWipeConfirmation: Boolean = false,
    val isWiping: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataSource: AscendPreferencesDataSource,
    private val dataExportManager: DataExportManager,
    private val vaultFileStorage: VaultFileStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesDataSource.userPreferencesFlow.collect { prefs ->
                _uiState.update { it.copy(userPreferences = prefs) }
            }
        }
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

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val json = dataExportManager.exportDataAsJson()
            _uiState.update { it.copy(isExporting = false, exportedJson = json) }
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
            _uiState.update { it.copy(isWiping = false) }
            onWiped()
        }
    }
}

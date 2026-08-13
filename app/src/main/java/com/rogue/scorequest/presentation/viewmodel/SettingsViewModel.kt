package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.ExportResult
import com.rogue.scorequest.domain.usecase.ExportGamesUseCase
import com.rogue.scorequest.domain.usecase.GetThemePreferenceUseCase
import com.rogue.scorequest.domain.usecase.ImportSeedGamesUseCase
import com.rogue.scorequest.domain.usecase.SetThemePreferenceUseCase
import com.rogue.scorequest.presentation.viewmodel.states.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    getThemePreference: GetThemePreferenceUseCase,
    private val setThemePreference: SetThemePreferenceUseCase,
    private val importSeedGames: ImportSeedGamesUseCase,
    private val exportGames: ExportGamesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getThemePreference().collect { isDark ->
                _state.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }

    fun onThemeToggled(enabled: Boolean) {
        viewModelScope.launch { setThemePreference(enabled) }
    }

    fun onJsonSelected(content: String) {
        if (_state.value.isImporting) return
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, importResult = null, importReadError = null) }
            val result = importSeedGames(content)
            _state.update { it.copy(isImporting = false, importResult = result) }
        }
    }

    fun onJsonReadError(message: String) {
        _state.update { it.copy(importReadError = message) }
    }

    fun dismissImportResult() {
        _state.update { it.copy(importResult = null) }
    }

    fun dismissImportReadError() {
        _state.update { it.copy(importReadError = null) }
    }

    fun onExportRequested() {
        if (_state.value.isExporting) return
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            val result = exportGames()
            _state.update { it.copy(isExporting = false, pendingExport = result) }
        }
    }

    fun onExportLaunched() {
        _state.update { it.copy(pendingExport = null) }
    }

    fun onExportWritten(result: ExportResult) {
        _state.update {
            it.copy(exportSuccessMessage = "${result.gamesCount} jogo(s) e ${result.schemasCount} pontuação(ões) exportados.")
        }
    }

    fun onExportError(message: String) {
        _state.update { it.copy(exportError = message) }
    }

    fun dismissExportSuccess() {
        _state.update { it.copy(exportSuccessMessage = null) }
    }

    fun dismissExportError() {
        _state.update { it.copy(exportError = null) }
    }
}

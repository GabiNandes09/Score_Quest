package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val importSeedGames: ImportSeedGamesUseCase
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
}

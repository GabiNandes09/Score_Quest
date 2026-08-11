package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.usecase.GetThemePreferenceUseCase
import com.rogue.scorequest.domain.usecase.SetThemePreferenceUseCase
import com.rogue.scorequest.presentation.viewmodel.states.SettingsState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    getThemePreference: GetThemePreferenceUseCase,
    private val setThemePreference: SetThemePreferenceUseCase
) : ViewModel() {

    val state = getThemePreference()
        .map { SettingsState(isDarkTheme = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun onThemeToggled(enabled: Boolean) {
        viewModelScope.launch { setThemePreference(enabled) }
    }
}

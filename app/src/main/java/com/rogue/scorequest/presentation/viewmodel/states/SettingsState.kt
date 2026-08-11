package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.ImportResult

data class SettingsState(
    val isDarkTheme: Boolean = true,
    val isImporting: Boolean = false,
    val importResult: ImportResult? = null,
    val importReadError: String? = null
)

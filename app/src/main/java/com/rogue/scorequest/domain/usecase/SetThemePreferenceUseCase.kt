package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.local.ThemePreferences

class SetThemePreferenceUseCase(
    private val preferences: ThemePreferences
) {
    suspend operator fun invoke(enabled: Boolean) = preferences.setDarkTheme(enabled)
}

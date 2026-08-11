package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.local.ThemePreferences
import kotlinx.coroutines.flow.Flow

class GetThemePreferenceUseCase(
    private val preferences: ThemePreferences
) {
    operator fun invoke(): Flow<Boolean> = preferences.isDarkTheme
}

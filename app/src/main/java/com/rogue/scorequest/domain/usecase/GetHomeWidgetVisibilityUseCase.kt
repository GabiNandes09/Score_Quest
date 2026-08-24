package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.local.ThemePreferences
import com.rogue.scorequest.domain.model.HomeWidget
import kotlinx.coroutines.flow.Flow

class GetHomeWidgetVisibilityUseCase(
    private val preferences: ThemePreferences
) {
    operator fun invoke(): Flow<Set<HomeWidget>> = preferences.visibleHomeWidgets
}

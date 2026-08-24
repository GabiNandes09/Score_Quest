package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.local.ThemePreferences
import com.rogue.scorequest.domain.model.HomeWidget

class SetHomeWidgetVisibleUseCase(
    private val preferences: ThemePreferences
) {
    suspend operator fun invoke(widget: HomeWidget, visible: Boolean) =
        preferences.setHomeWidgetVisible(widget, visible)
}

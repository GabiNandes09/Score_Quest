package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.BoardGame

enum class ProfileTab {
    FAVORITES, ACTIVITIES
}

data class ProfileState(
    val displayName: String = "",
    val bio: String? = null,
    val avatarUri: String? = null,
    val favoriteGames: List<BoardGame> = emptyList(),
    val sessionCount: Int = 0,
    val selectedTab: ProfileTab = ProfileTab.FAVORITES
)

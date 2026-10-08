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
    val selectedTab: ProfileTab = ProfileTab.FAVORITES,
    // Vêm da conta Google (ver AuthUser) — authDisplayName/authPhotoUrl só servem de
    // fallback quando o perfil local (displayName/avatarUri acima) ainda está vazio;
    // username não tem equivalente local, só existe se a conta estiver logada.
    val authDisplayName: String? = null,
    val authPhotoUrl: String? = null,
    val username: String? = null
)

package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.rogue.scorequest.domain.usecase.GetFavoriteGamesUseCase
import com.rogue.scorequest.domain.usecase.GetProfileUseCase
import com.rogue.scorequest.domain.usecase.GetSessionCountUseCase
import com.rogue.scorequest.domain.usecase.GetSessionsPagedUseCase
import com.rogue.scorequest.presentation.viewmodel.states.ProfileState
import com.rogue.scorequest.presentation.viewmodel.states.ProfileTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    getProfile: GetProfileUseCase,
    getFavoriteGames: GetFavoriteGamesUseCase,
    getSessionCount: GetSessionCountUseCase,
    getSessionsPaged: GetSessionsPagedUseCase
) : ViewModel() {

    private val selectedTab = MutableStateFlow(ProfileTab.FAVORITES)

    val state = combine(
        getProfile(),
        getFavoriteGames(),
        getSessionCount(),
        selectedTab
    ) { profile, favorites, count, tab ->
        ProfileState(
            displayName = profile?.displayName.orEmpty(),
            bio = profile?.bio,
            avatarUri = profile?.avatarUri,
            favoriteGames = favorites,
            sessionCount = count,
            selectedTab = tab
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileState())

    val pagedSessions = getSessionsPaged().cachedIn(viewModelScope)

    fun onTabSelected(tab: ProfileTab) {
        selectedTab.value = tab
    }
}

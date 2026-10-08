package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.rogue.scorequest.domain.model.BoardGame
import com.rogue.scorequest.domain.model.UserProfile
import com.rogue.scorequest.domain.usecase.GetAuthStateUseCase
import com.rogue.scorequest.domain.usecase.GetFavoriteGamesUseCase
import com.rogue.scorequest.domain.usecase.GetProfileUseCase
import com.rogue.scorequest.domain.usecase.GetPublicUsernameUseCase
import com.rogue.scorequest.domain.usecase.GetSessionCountUseCase
import com.rogue.scorequest.domain.usecase.GetSessionsPagedUseCase
import com.rogue.scorequest.presentation.viewmodel.states.ProfileState
import com.rogue.scorequest.presentation.viewmodel.states.ProfileTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class CoreProfileData(
    val profile: UserProfile?,
    val favorites: List<BoardGame>,
    val sessionCount: Int,
    val tab: ProfileTab
)

class ProfileViewModel(
    getProfile: GetProfileUseCase,
    getFavoriteGames: GetFavoriteGamesUseCase,
    getSessionCount: GetSessionCountUseCase,
    getSessionsPaged: GetSessionsPagedUseCase,
    getAuthState: GetAuthStateUseCase,
    private val getPublicUsername: GetPublicUsernameUseCase
) : ViewModel() {

    private val selectedTab = MutableStateFlow(ProfileTab.FAVORITES)
    private val username = MutableStateFlow<String?>(null)

    private val authUser = getAuthState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // @username não é reativo (gravado uma única vez no login, ver
        // EnsurePublicProfileUseCase) — busca sob demanda só quando o uid muda,
        // em vez de expor mais um Flow direto do Firestore pra isso.
        viewModelScope.launch {
            var lastUid: String? = null
            authUser.collect { user ->
                if (user?.uid != lastUid) {
                    lastUid = user?.uid
                    username.value = user?.let { getPublicUsername(it.uid) }
                }
            }
        }
    }

    val state = combine(
        combine(getProfile(), getFavoriteGames(), getSessionCount(), selectedTab) { profile, favorites, count, tab ->
            CoreProfileData(profile, favorites, count, tab)
        },
        authUser,
        username
    ) { core, user, name ->
        ProfileState(
            displayName = core.profile?.displayName.orEmpty(),
            bio = core.profile?.bio,
            avatarUri = core.profile?.avatarUri,
            favoriteGames = core.favorites,
            sessionCount = core.sessionCount,
            selectedTab = core.tab,
            authDisplayName = user?.displayName,
            authPhotoUrl = user?.photoUrl,
            username = name
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileState())

    val pagedSessions = getSessionsPaged().cachedIn(viewModelScope)

    fun onTabSelected(tab: ProfileTab) {
        selectedTab.value = tab
    }
}

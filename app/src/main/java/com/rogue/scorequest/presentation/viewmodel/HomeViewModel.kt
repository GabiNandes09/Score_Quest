package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.usecase.GetHomeStatsUseCase
import com.rogue.scorequest.domain.usecase.GetMonthlyPlaytimeUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.GetProfileUseCase
import com.rogue.scorequest.domain.usecase.GetRecentSessionsUseCase
import com.rogue.scorequest.domain.usecase.GetSessionCountUseCase
import com.rogue.scorequest.presentation.viewmodel.states.HomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val LAST_SESSION_LIMIT = 1

class HomeViewModel(
    getHomeStats: GetHomeStatsUseCase,
    getRecentSessions: GetRecentSessionsUseCase,
    getProfile: GetProfileUseCase,
    getSessionCount: GetSessionCountUseCase,
    getPlayers: GetPlayersUseCase,
    getMonthlyPlaytime: GetMonthlyPlaytimeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getProfile().collect { profile ->
                _state.value = _state.value.copy(displayName = profile?.displayName.orEmpty())
            }
        }
        viewModelScope.launch {
            getHomeStats().collect { stats ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    streakDays = stats.streakDays,
                    isStreakActive = stats.isStreakActive,
                    totalMinutes = stats.totalMinutes,
                    topGames = stats.topGames
                )
            }
        }
        viewModelScope.launch {
            getRecentSessions(LAST_SESSION_LIMIT).collect { sessions ->
                _state.value = _state.value.copy(lastSession = sessions.firstOrNull())
            }
        }
        viewModelScope.launch {
            getMonthlyPlaytime().collect { monthly ->
                _state.value = _state.value.copy(monthlyPlaytime = monthly)
            }
        }
        viewModelScope.launch {
            getSessionCount().collect { count ->
                _state.value = _state.value.copy(totalSessions = count)
            }
        }
        viewModelScope.launch {
            getPlayers().collect { players ->
                _state.value = _state.value.copy(players = players)
            }
        }
    }
}

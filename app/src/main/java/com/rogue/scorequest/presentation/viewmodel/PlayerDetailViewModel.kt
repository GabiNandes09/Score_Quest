package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.usecase.GetPlayerStatsUseCase
import com.rogue.scorequest.domain.usecase.GetPlayerUseCase
import com.rogue.scorequest.presentation.viewmodel.states.PlayerDetailState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PlayerDetailViewModel(
    playerId: String,
    getPlayer: GetPlayerUseCase,
    getPlayerStats: GetPlayerStatsUseCase
) : ViewModel() {

    val state = combine(
        getPlayer(playerId),
        getPlayerStats(playerId)
    ) { player, stats ->
        PlayerDetailState(isLoading = false, player = player, stats = stats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerDetailState())
}

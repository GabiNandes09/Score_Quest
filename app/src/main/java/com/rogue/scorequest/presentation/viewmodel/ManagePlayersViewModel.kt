package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.PlayerGroup
import com.rogue.scorequest.domain.usecase.GetPlayerGroupsUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ManagePlayersViewModel(
    getPlayers: GetPlayersUseCase,
    getPlayerGroups: GetPlayerGroupsUseCase
) : ViewModel() {

    val players: StateFlow<List<Player>> = getPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<PlayerGroup>> = getPlayerGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

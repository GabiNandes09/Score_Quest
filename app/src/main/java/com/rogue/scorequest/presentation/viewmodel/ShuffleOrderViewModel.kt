package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.PlayerGroup
import com.rogue.scorequest.domain.usecase.GetPlayerGroupsUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.presentation.viewmodel.states.ShuffleOrderState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class ShuffleOrderViewModel(
    getPlayers: GetPlayersUseCase,
    getPlayerGroups: GetPlayerGroupsUseCase
) : ViewModel() {

    val players: StateFlow<List<Player>> = getPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<PlayerGroup>> = getPlayerGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _state = MutableStateFlow(ShuffleOrderState())
    val state = _state.asStateFlow()

    fun onPlayerToggled(playerId: String) {
        val current = _state.value.selectedPlayerIds
        val updated = if (playerId in current) current - playerId else current + playerId
        _state.value = _state.value.copy(selectedPlayerIds = updated, selectedGroupId = null)
    }

    fun onGroupSelected(group: PlayerGroup) {
        _state.value = _state.value.copy(
            selectedPlayerIds = group.memberIds.toSet(),
            selectedGroupId = group.id
        )
    }
}

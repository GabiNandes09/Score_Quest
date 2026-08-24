package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.usecase.GetGroupStatsUseCase
import com.rogue.scorequest.domain.usecase.GetPlayerGroupUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.presentation.viewmodel.states.GroupDetailState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class GroupDetailViewModel(
    groupId: String,
    getGroup: GetPlayerGroupUseCase,
    getPlayers: GetPlayersUseCase,
    getGroupStats: GetGroupStatsUseCase
) : ViewModel() {

    val state = combine(
        getGroup(groupId),
        getPlayers(),
        getGroupStats(groupId)
    ) { group, players, stats ->
        GroupDetailState(
            isLoading = false,
            group = group,
            members = players.filter { it.id in (group?.memberIds ?: emptyList()) },
            stats = stats
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupDetailState())
}

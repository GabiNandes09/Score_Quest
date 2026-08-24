package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.GroupStats
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.PlayerGroup

data class GroupDetailState(
    val isLoading: Boolean = true,
    val group: PlayerGroup? = null,
    val members: List<Player> = emptyList(),
    val stats: GroupStats? = null
)

package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.PlayerStats

data class PlayerDetailState(
    val isLoading: Boolean = true,
    val player: Player? = null,
    val stats: PlayerStats? = null
)

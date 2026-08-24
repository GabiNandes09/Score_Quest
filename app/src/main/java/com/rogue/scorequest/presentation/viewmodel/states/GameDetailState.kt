package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.ActiveTimer
import com.rogue.scorequest.domain.model.GameStats
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.domain.model.SessionWithDetails

data class GameDetailState(
    val isLoading: Boolean = true,
    val game: GameWithLibraryInfo? = null,
    val stats: GameStats? = null,
    val sessions: List<SessionWithDetails> = emptyList(),
    val hasScoreSchema: Boolean = false,
    val activeTimer: ActiveTimer? = null
)

package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.GamePlayCount
import com.rogue.scorequest.domain.model.MonthlyPlaytime
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.SessionWithDetails

data class HomeState(
    val isLoading: Boolean = true,
    val displayName: String = "",
    val streakDays: Int = 0,
    val isStreakActive: Boolean = false,
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val topGames: List<GamePlayCount> = emptyList(),
    val monthlyPlaytime: List<MonthlyPlaytime> = emptyList(),
    val lastSession: SessionWithDetails? = null,
    val players: List<Player> = emptyList()
)

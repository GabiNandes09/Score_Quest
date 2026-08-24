package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.ActiveTimer
import com.rogue.scorequest.domain.model.DayActivity
import com.rogue.scorequest.domain.model.DurationBucket
import com.rogue.scorequest.domain.model.GamePlayCount
import com.rogue.scorequest.domain.model.HomeWidget
import com.rogue.scorequest.domain.model.MonthSessionCount
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.PlayerWinCount
import com.rogue.scorequest.domain.model.SessionWithDetails

data class HomeState(
    val isLoading: Boolean = true,
    val displayName: String = "",
    val streakDays: Int = 0,
    val isStreakActive: Boolean = false,
    val totalSessions: Int = 0,
    val weekMinutes: Int = 0,
    val totalMinutes: Int = 0,
    val topGames: List<GamePlayCount> = emptyList(),
    val topPlayersByWins: List<PlayerWinCount> = emptyList(),
    val activityHeatmap: List<DayActivity> = emptyList(),
    val sessionsByMonth: List<MonthSessionCount> = emptyList(),
    val durationHistogram: List<DurationBucket> = emptyList(),
    val lastSession: SessionWithDetails? = null,
    val players: List<Player> = emptyList(),
    val activeTimer: ActiveTimer? = null,
    val visibleWidgets: Set<HomeWidget> = HomeWidget.entries.toSet()
)

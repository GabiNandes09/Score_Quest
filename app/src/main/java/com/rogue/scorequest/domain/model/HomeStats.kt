package com.rogue.scorequest.domain.model

data class GamePlayCount(
    val gameId: String,
    val gameName: String,
    val playCount: Int
)

data class HomeStats(
    val streakDays: Int,
    val isStreakActive: Boolean,
    val topGames: List<GamePlayCount>,
    val weekMinutes: Int,
    val totalMinutes: Int
)

package com.rogue.scorequest.domain.model

data class GamePlayCount(
    val gameId: String,
    val gameName: String,
    val playCount: Int
)

data class PlayerWinCount(
    val playerId: String,
    val playerName: String,
    val wins: Int
)

data class HomeStats(
    val streakDays: Int,
    val isStreakActive: Boolean,
    val topGames: List<GamePlayCount>,
    val topPlayersByWins: List<PlayerWinCount>,
    val weekMinutes: Int,
    val totalMinutes: Int
)

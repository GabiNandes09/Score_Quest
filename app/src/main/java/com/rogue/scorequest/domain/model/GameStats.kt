package com.rogue.scorequest.domain.model

data class GameStats(
    val timesPlayed: Int,
    val avgDurationMinutes: Int,
    val longestSessionMinutes: Int?,
    val highScore: Int?,
    val topPlayersByPlays: List<PlayerPlayCount>,
    val topPlayersByWins: List<PlayerWinCount>,
    val topScores: List<GameScoreRecord>
)

data class GameScoreRecord(
    val sessionId: String,
    val playerId: String,
    val playerName: String,
    val score: Int,
    val date: Long
)

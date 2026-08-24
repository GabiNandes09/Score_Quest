package com.rogue.scorequest.domain.model

data class GroupStats(
    val gamesPlayed: Int,
    val totalMinutes: Int,
    val topGames: List<GamePlayCount>,
    val favoriteGame: GamePlayCount?,
    val memberWins: List<PlayerWinCount>
)

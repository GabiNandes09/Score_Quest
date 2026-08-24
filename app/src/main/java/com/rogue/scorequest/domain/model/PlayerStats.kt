package com.rogue.scorequest.domain.model

data class PlayerStats(
    val gamesPlayed: Int,
    val wins: Int,
    val decidedGames: Int, // partidas com vencedor definido (winnerMode != NONE) — denominador da taxa
    val winRate: Double?, // wins / decidedGames; null se decidedGames == 0 (só partidas cooperativas, por exemplo)
    val totalMinutes: Int,
    val currentWinStreak: Int,
    val bestWinStreak: Int,
    val topGames: List<GamePlayCount>, // jogos mais jogados por esse jogador (playCount = nº de partidas)
    val favoriteGame: GamePlayCount? // jogo com mais vitórias (playCount = nº de vitórias nesse jogo)
)

// Projeções internas de consulta (Room), usadas só pra montar PlayerStats no repository —
// não representam entidades de domínio próprias.
data class PlayerCoreCounts(
    val gamesPlayed: Int,
    val wins: Int,
    val decidedGames: Int
)

data class PlayerSessionResult(
    val date: Long,
    val isWinner: Boolean?
)

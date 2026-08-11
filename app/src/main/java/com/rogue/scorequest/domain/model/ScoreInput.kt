package com.rogue.scorequest.domain.model

data class ScoreInput(
    val playerId: String,
    val totalScore: Int?,
    val isWinner: Boolean?
)

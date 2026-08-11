package com.rogue.scorequest.domain.model

data class GameStats(
    val timesPlayed: Int,
    val avgDurationMinutes: Int,
    val highScore: Int?
)

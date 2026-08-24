package com.rogue.scorequest.domain.model

data class SessionWithDetails(
    val session: GameSession,
    val gameName: String,
    val gameCoverImageUrl: String?,
    val groupName: String?,
    val scores: List<ScoreEntry>
)

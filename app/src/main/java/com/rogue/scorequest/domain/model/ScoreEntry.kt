package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class ScoreEntry(
    val sessionId: String,
    val playerId: String,
    val totalScore: Int?,
    val isWinner: Boolean?,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

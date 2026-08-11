package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class GameSession(
    val id: String,
    val gameId: String,
    val date: LocalDateTime,
    val durationMinutes: Int,
    val variantOrExpansion: String? = null,
    val photoUri: String? = null,
    val participantIds: List<String>,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

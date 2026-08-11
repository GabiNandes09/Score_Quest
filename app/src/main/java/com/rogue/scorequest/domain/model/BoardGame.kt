package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class BoardGame(
    val id: String,
    val name: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val avgDurationMinutes: Int,
    val coverImageUrl: String?,
    val category: String?,
    val weight: Double?,
    val source: GameSource,
    val createdByUserId: String? = null,
    val syncedAt: LocalDateTime? = null,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

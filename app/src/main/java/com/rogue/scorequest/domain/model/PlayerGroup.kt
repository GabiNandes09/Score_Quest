package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class PlayerGroup(
    val id: String,
    val name: String,
    val photoPath: String?,
    val memberIds: List<String>,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

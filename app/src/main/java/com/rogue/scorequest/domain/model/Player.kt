package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class Player(
    val id: String,
    val nickname: String,
    val linkedUserId: String? = null,
    val avatarColor: String?,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

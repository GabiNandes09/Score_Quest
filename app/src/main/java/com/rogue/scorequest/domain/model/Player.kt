package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class Player(
    val id: String,
    val nickname: String,
    val linkedUserId: String? = null,
    // Caminho local ou URL da foto de perfil — mesma dualidade de BoardGame.coverImageUrl.
    // Reaproveita a coluna "avatar_color" (nunca usada pra cor de verdade) pra não precisar
    // de migração de banco; só o nome do campo Kotlin mudou, ver PlayerEntity.kt.
    val avatarPath: String?,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.utils.toEpochMillis
import com.rogue.scorequest.utils.toLocalDateTime

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val nickname: String,
    @ColumnInfo(name = "linked_user_id") val linkedUserId: String?,
    // Nome da coluna ficou "avatar_color" por histórico (campo nunca usado antes) — não
    // renomear a coluna em si evita uma migração de Room; ver Player.kt.
    @ColumnInfo(name = "avatar_color") val avatarPath: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?
)

fun PlayerEntity.toDomain() = Player(
    id = id,
    nickname = nickname,
    linkedUserId = linkedUserId,
    avatarPath = avatarPath,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun Player.toEntity() = PlayerEntity(
    id = id,
    nickname = nickname,
    linkedUserId = linkedUserId,
    avatarPath = avatarPath,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

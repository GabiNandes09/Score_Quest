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
    @ColumnInfo(name = "avatar_color") val avatarColor: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?
)

fun PlayerEntity.toDomain() = Player(
    id = id,
    nickname = nickname,
    linkedUserId = linkedUserId,
    avatarColor = avatarColor,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun Player.toEntity() = PlayerEntity(
    id = id,
    nickname = nickname,
    linkedUserId = linkedUserId,
    avatarColor = avatarColor,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

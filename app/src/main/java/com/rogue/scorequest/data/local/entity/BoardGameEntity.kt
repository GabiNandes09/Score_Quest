package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rogue.scorequest.domain.model.BoardGame
import com.rogue.scorequest.domain.model.GameSource
import com.rogue.scorequest.utils.toEpochMillis
import com.rogue.scorequest.utils.toLocalDateTime

@Entity(tableName = "board_game")
data class BoardGameEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "min_players") val minPlayers: Int,
    @ColumnInfo(name = "max_players") val maxPlayers: Int,
    @ColumnInfo(name = "avg_duration_minutes") val avgDurationMinutes: Int,
    @ColumnInfo(name = "cover_image_url") val coverImageUrl: String?,
    val category: String?,
    val weight: Double?,
    val source: GameSource,
    @ColumnInfo(name = "created_by_user_id") val createdByUserId: String?,
    @ColumnInfo(name = "synced_at") val syncedAt: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?
)

fun BoardGameEntity.toDomain() = BoardGame(
    id = id,
    name = name,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    avgDurationMinutes = avgDurationMinutes,
    coverImageUrl = coverImageUrl,
    category = category,
    weight = weight,
    source = source,
    createdByUserId = createdByUserId,
    syncedAt = syncedAt?.toLocalDateTime(),
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun BoardGame.toEntity() = BoardGameEntity(
    id = id,
    name = name,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    avgDurationMinutes = avgDurationMinutes,
    coverImageUrl = coverImageUrl,
    category = category,
    weight = weight,
    source = source,
    createdByUserId = createdByUserId,
    syncedAt = syncedAt?.toEpochMillis(),
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

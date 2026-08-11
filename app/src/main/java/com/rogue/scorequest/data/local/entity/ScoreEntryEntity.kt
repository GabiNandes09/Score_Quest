package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.rogue.scorequest.domain.model.ScoreEntry
import com.rogue.scorequest.utils.toEpochMillis
import com.rogue.scorequest.utils.toLocalDateTime

@Entity(
    tableName = "score_entry",
    primaryKeys = ["session_id", "player_id"],
    foreignKeys = [
        ForeignKey(
            entity = GameSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"]
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"]
        )
    ],
    indices = [Index("session_id"), Index("player_id")]
)
data class ScoreEntryEntity(
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "player_id") val playerId: String,
    @ColumnInfo(name = "total_score") val totalScore: Int?,
    @ColumnInfo(name = "is_winner") val isWinner: Boolean?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?
)

fun ScoreEntryEntity.toDomain() = ScoreEntry(
    sessionId = sessionId,
    playerId = playerId,
    totalScore = totalScore,
    isWinner = isWinner,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun ScoreEntry.toEntity() = ScoreEntryEntity(
    sessionId = sessionId,
    playerId = playerId,
    totalScore = totalScore,
    isWinner = isWinner,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

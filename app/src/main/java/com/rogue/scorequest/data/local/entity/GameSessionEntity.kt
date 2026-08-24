package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rogue.scorequest.domain.model.GameSession
import com.rogue.scorequest.utils.toEpochMillis
import com.rogue.scorequest.utils.toLocalDateTime

@Entity(
    tableName = "game_session",
    foreignKeys = [
        ForeignKey(
            entity = BoardGameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"]
        )
    ],
    indices = [Index("game_id"), Index("group_id")]
)
data class GameSessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "game_id") val gameId: String,
    val date: Long,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    @ColumnInfo(name = "variant_or_expansion") val variantOrExpansion: String?,
    @ColumnInfo(name = "photo_uri") val photoUri: String?,
    // Referência solta pro grupo usado ao registrar a partida — de propósito SEM ForeignKey
    // (coluna adicionada via ALTER TABLE numa migração; SQLite não permite acrescentar
    // constraint FK depois que a tabela já existe). Se o grupo for excluído depois, essa
    // coluna some junto (fica um id órfão) e a UI trata isso como "sem grupo" — aceitável,
    // já que é só um dado apresentacional (nome do grupo no detalhe da partida), não histórico
    // de verdade. Ver CLAUDE.md "Grupos de jogadores".
    @ColumnInfo(name = "group_id") val groupId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?
)

fun GameSessionEntity.toDomain(participantIds: List<String>) = GameSession(
    id = id,
    gameId = gameId,
    date = date.toLocalDateTime(),
    durationMinutes = durationMinutes,
    variantOrExpansion = variantOrExpansion,
    photoUri = photoUri,
    groupId = groupId,
    participantIds = participantIds,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun GameSession.toEntity() = GameSessionEntity(
    id = id,
    gameId = gameId,
    date = date.toEpochMillis(),
    durationMinutes = durationMinutes,
    variantOrExpansion = variantOrExpansion,
    photoUri = photoUri,
    groupId = groupId,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

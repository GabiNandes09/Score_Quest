package com.rogue.scorequest.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.rogue.scorequest.domain.model.SessionWithDetails

data class SessionWithScoresEntity(
    @Embedded val session: GameSessionEntity,
    @Relation(parentColumn = "game_id", entityColumn = "id")
    val game: BoardGameEntity,
    @Relation(parentColumn = "id", entityColumn = "session_id")
    val scores: List<ScoreEntryEntity>
)

fun SessionWithScoresEntity.toDomain() = SessionWithDetails(
    session = session.toDomain(scores.map { it.playerId }),
    gameName = game.name,
    gameCoverImageUrl = game.coverImageUrl,
    scores = scores.map { it.toDomain() }
)

package com.rogue.scorequest.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.rogue.scorequest.domain.model.SessionWithDetails

data class SessionWithScoresEntity(
    @Embedded val session: GameSessionEntity,
    @Relation(parentColumn = "game_id", entityColumn = "id")
    val game: BoardGameEntity,
    @Relation(parentColumn = "id", entityColumn = "session_id")
    val scores: List<ScoreEntryEntity>,
    // Nullable de propósito — group_id não tem ForeignKey (ver GameSessionEntity), então o
    // Room resolve isso só por uma segunda SELECT WHERE id IN (...), sem exigir a constraint.
    @Relation(parentColumn = "group_id", entityColumn = "id")
    val group: PlayerGroupEntity?
)

fun SessionWithScoresEntity.toDomain() = SessionWithDetails(
    session = session.toDomain(scores.map { it.playerId }),
    gameName = game.name,
    gameCoverImageUrl = game.coverImageUrl,
    groupName = group?.name,
    scores = scores.map { it.toDomain() }
)

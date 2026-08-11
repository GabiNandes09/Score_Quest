package com.rogue.scorequest.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.rogue.scorequest.domain.model.GameWithLibraryInfo

data class GameWithLibraryEntryEntity(
    @Embedded val game: BoardGameEntity,
    @Relation(parentColumn = "id", entityColumn = "game_id")
    val libraryEntry: UserLibraryEntryEntity?
)

fun GameWithLibraryEntryEntity.toDomain() = GameWithLibraryInfo(
    game = game.toDomain(),
    libraryEntry = libraryEntry?.toDomain()
)

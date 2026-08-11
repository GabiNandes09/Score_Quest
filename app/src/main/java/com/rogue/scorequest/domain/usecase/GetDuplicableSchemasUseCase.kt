package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.data.repository.GameScoreSchemaRepository
import com.rogue.scorequest.domain.model.DuplicableSchema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetDuplicableSchemasUseCase(
    private val schemaRepository: GameScoreSchemaRepository,
    private val gameRepository: BoardGameRepository
) {
    operator fun invoke(): Flow<List<DuplicableSchema>> =
        combine(schemaRepository.getAllCompositeSchemas(), gameRepository.getGames()) { schemas, games ->
            val namesById = games.associate { it.game.id to it.game.name }
            schemas.mapNotNull { schema ->
                namesById[schema.gameId]?.let { name -> DuplicableSchema(schema = schema, gameName = name) }
            }
        }
}

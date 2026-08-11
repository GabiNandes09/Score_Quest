package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameScoreSchemaRepository
import com.rogue.scorequest.domain.model.GameScoreSchema
import kotlinx.coroutines.flow.Flow

class GetGameScoreSchemaUseCase(
    private val repository: GameScoreSchemaRepository
) {
    operator fun invoke(gameId: String): Flow<GameScoreSchema?> = repository.getByGameId(gameId)
}

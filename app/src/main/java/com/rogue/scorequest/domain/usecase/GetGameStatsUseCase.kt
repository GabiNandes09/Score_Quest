package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.GameStats
import kotlinx.coroutines.flow.Flow

class GetGameStatsUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(gameId: String): Flow<GameStats> = repository.getGameStats(gameId)
}

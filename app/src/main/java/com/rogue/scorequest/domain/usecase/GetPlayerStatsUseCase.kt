package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.PlayerStats
import kotlinx.coroutines.flow.Flow

class GetPlayerStatsUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(playerId: String): Flow<PlayerStats> = repository.getPlayerStats(playerId)
}

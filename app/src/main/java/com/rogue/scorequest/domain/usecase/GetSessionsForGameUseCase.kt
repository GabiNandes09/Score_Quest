package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.SessionWithDetails
import kotlinx.coroutines.flow.Flow

class GetSessionsForGameUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(gameId: String): Flow<List<SessionWithDetails>> = repository.getSessionsForGame(gameId)
}

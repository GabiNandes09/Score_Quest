package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.SessionWithDetails
import kotlinx.coroutines.flow.Flow

class GetRecentSessionsUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(limit: Int = 3): Flow<List<SessionWithDetails>> = repository.getRecentSessions(limit)
}

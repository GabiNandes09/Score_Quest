package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.SessionWithDetails
import kotlinx.coroutines.flow.Flow

class GetSessionDetailUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(sessionId: String): Flow<SessionWithDetails?> = repository.getSessionDetail(sessionId)
}

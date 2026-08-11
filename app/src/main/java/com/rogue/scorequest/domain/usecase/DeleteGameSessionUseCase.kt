package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository

class DeleteGameSessionUseCase(
    private val repository: GameSessionRepository
) {
    suspend operator fun invoke(sessionId: String) =
        repository.deleteSession(sessionId, System.currentTimeMillis())
}

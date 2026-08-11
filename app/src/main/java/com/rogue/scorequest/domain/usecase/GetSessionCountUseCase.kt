package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import kotlinx.coroutines.flow.Flow

class GetSessionCountUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(): Flow<Int> = repository.getSessionCount()
}

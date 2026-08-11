package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import kotlinx.coroutines.flow.Flow

class GetLastPlayedDatesUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(): Flow<Map<String, Long>> = repository.getLastPlayedDates()
}

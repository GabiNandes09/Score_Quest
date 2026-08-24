package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ActiveTimerRepository

class ClearActiveTimerForGameUseCase(
    private val repository: ActiveTimerRepository
) {
    suspend operator fun invoke(gameId: String) = repository.clearIfGameMatches(gameId)
}

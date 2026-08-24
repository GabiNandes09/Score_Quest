package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ActiveTimerRepository

class CancelTimerUseCase(
    private val repository: ActiveTimerRepository
) {
    suspend operator fun invoke() = repository.clear()
}

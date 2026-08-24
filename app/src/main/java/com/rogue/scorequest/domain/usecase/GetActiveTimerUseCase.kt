package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ActiveTimerRepository
import com.rogue.scorequest.domain.model.ActiveTimer
import kotlinx.coroutines.flow.Flow

class GetActiveTimerUseCase(
    private val repository: ActiveTimerRepository
) {
    operator fun invoke(): Flow<ActiveTimer?> = repository.observe()
}

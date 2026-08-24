package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ActiveTimerRepository
import com.rogue.scorequest.domain.model.TimerStatus

class PauseTimerUseCase(
    private val repository: ActiveTimerRepository
) {
    suspend operator fun invoke() {
        val current = repository.getOnce() ?: return
        if (current.status != TimerStatus.RUNNING) return
        repository.save(current.copy(status = TimerStatus.PAUSED, pausedAtMillis = System.currentTimeMillis()))
    }
}

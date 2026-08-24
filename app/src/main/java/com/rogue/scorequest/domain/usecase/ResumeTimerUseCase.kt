package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ActiveTimerRepository
import com.rogue.scorequest.domain.model.TimerStatus

class ResumeTimerUseCase(
    private val repository: ActiveTimerRepository
) {
    suspend operator fun invoke() {
        val current = repository.getOnce() ?: return
        if (current.status != TimerStatus.PAUSED) return
        val pausedAt = current.pausedAtMillis ?: return
        val now = System.currentTimeMillis()
        repository.save(
            current.copy(
                status = TimerStatus.RUNNING,
                pausedAtMillis = null,
                accumulatedPausedMillis = current.accumulatedPausedMillis + (now - pausedAt)
            )
        )
    }
}

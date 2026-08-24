package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ActiveTimerRepository
import com.rogue.scorequest.domain.model.ActiveTimer
import com.rogue.scorequest.domain.model.TimerStatus

class StartTimerUseCase(
    private val repository: ActiveTimerRepository
) {
    suspend operator fun invoke(gameId: String, gameName: String) {
        repository.save(
            ActiveTimer(
                gameId = gameId,
                gameName = gameName,
                startedAtMillis = System.currentTimeMillis(),
                status = TimerStatus.RUNNING
            )
        )
    }
}

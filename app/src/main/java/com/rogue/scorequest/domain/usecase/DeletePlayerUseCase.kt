package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerRepository

class DeletePlayerUseCase(
    private val repository: PlayerRepository
) {
    suspend operator fun invoke(playerId: String): Boolean {
        if (repository.hasHistory(playerId)) return false
        repository.deletePlayer(playerId, System.currentTimeMillis())
        return true
    }
}

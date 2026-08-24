package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerRepository
import com.rogue.scorequest.domain.model.Player
import kotlinx.coroutines.flow.Flow

class GetPlayerUseCase(
    private val repository: PlayerRepository
) {
    operator fun invoke(playerId: String): Flow<Player?> = repository.getPlayer(playerId)
}

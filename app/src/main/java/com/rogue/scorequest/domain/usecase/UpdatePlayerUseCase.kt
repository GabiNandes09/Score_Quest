package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerRepository
import com.rogue.scorequest.domain.model.Player
import java.time.LocalDateTime

class UpdatePlayerUseCase(
    private val repository: PlayerRepository
) {
    suspend operator fun invoke(player: Player, newNickname: String) {
        repository.updatePlayer(player.copy(nickname = newNickname, updatedAt = LocalDateTime.now()))
    }
}

package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerRepository
import com.rogue.scorequest.domain.model.Player
import java.time.LocalDateTime
import java.util.UUID

class CreatePlayerUseCase(
    private val repository: PlayerRepository
) {
    suspend operator fun invoke(nickname: String, avatarColor: String? = null): Player {
        val now = LocalDateTime.now()
        val player = Player(
            id = UUID.randomUUID().toString(),
            nickname = nickname,
            avatarColor = avatarColor,
            createdAt = now,
            updatedAt = now
        )
        repository.insertPlayer(player)
        return player
    }
}

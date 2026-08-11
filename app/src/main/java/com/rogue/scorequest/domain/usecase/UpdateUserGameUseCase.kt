package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import java.time.LocalDateTime

class UpdateUserGameUseCase(
    private val repository: BoardGameRepository
) {
    suspend operator fun invoke(
        gameId: String,
        name: String,
        minPlayers: Int,
        maxPlayers: Int,
        avgDurationMinutes: Int,
        category: String?,
        weight: Double?,
        coverImageUrl: String?
    ) {
        val existing = repository.findGameOnce(gameId) ?: return
        repository.updateGame(
            existing.copy(
                name = name,
                minPlayers = minPlayers,
                maxPlayers = maxPlayers,
                avgDurationMinutes = avgDurationMinutes,
                category = category,
                weight = weight,
                coverImageUrl = coverImageUrl,
                updatedAt = LocalDateTime.now()
            )
        )
    }
}

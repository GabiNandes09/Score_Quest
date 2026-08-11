package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.domain.model.BoardGame
import com.rogue.scorequest.domain.model.GameSource
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.UserLibraryEntry
import java.time.LocalDateTime
import java.util.UUID

class AddUserGameUseCase(
    private val repository: BoardGameRepository
) {
    suspend operator fun invoke(
        name: String,
        minPlayers: Int,
        maxPlayers: Int,
        avgDurationMinutes: Int,
        category: String?,
        weight: Double?,
        coverImageUrl: String?,
        initialStatus: LibraryStatus
    ): String {
        val gameId = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val game = BoardGame(
            id = gameId,
            name = name,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            avgDurationMinutes = avgDurationMinutes,
            coverImageUrl = coverImageUrl,
            category = category,
            weight = weight,
            source = GameSource.USER_CREATED,
            createdAt = now,
            updatedAt = now
        )
        repository.insertGame(game)
        repository.upsertLibraryEntry(
            UserLibraryEntry(
                gameId = gameId,
                status = initialStatus,
                played = false,
                createdAt = now,
                updatedAt = now
            )
        )
        return gameId
    }
}

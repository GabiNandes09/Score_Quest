package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.UserLibraryEntry
import java.time.LocalDateTime

class RateGameUseCase(
    private val repository: BoardGameRepository
) {
    suspend operator fun invoke(gameId: String, rating: Int) {
        val now = LocalDateTime.now()
        val existing = repository.getLibraryEntryOnce(gameId)
        val entry = existing?.copy(rating = rating, updatedAt = now)
            ?: UserLibraryEntry(
                gameId = gameId,
                status = LibraryStatus.DONT_HAVE,
                played = true,
                rating = rating,
                createdAt = now,
                updatedAt = now
            )
        repository.upsertLibraryEntry(entry)
    }
}

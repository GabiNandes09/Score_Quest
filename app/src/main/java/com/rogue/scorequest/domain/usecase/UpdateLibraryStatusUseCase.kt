package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.UserLibraryEntry
import java.time.LocalDateTime

class UpdateLibraryStatusUseCase(
    private val repository: BoardGameRepository
) {
    suspend operator fun invoke(gameId: String, status: LibraryStatus) {
        val now = LocalDateTime.now()
        val existing = repository.getLibraryEntryOnce(gameId)
        val entry = existing?.copy(status = status, updatedAt = now)
            ?: UserLibraryEntry(gameId = gameId, status = status, createdAt = now, updatedAt = now)
        repository.upsertLibraryEntry(entry)
    }
}

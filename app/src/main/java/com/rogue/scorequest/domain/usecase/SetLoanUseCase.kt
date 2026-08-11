package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.UserLibraryEntry
import java.time.LocalDateTime

class SetLoanUseCase(
    private val repository: BoardGameRepository
) {
    suspend operator fun invoke(gameId: String, lentTo: String?) {
        val now = LocalDateTime.now()
        val existing = repository.getLibraryEntryOnce(gameId)
        val entry = existing?.copy(lentTo = lentTo, updatedAt = now)
            ?: UserLibraryEntry(gameId = gameId, status = LibraryStatus.HAVE, lentTo = lentTo, createdAt = now, updatedAt = now)
        repository.upsertLibraryEntry(entry)
    }
}

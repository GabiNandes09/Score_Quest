package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ProfileRepository

class SetFavoriteGameUseCase(
    private val repository: ProfileRepository
) {
    suspend fun add(gameId: String): Boolean {
        val count = repository.getFavoriteCount()
        if (count >= MAX_FAVORITES) return false
        repository.addFavorite(gameId)
        return true
    }

    suspend fun replaceOldestWith(gameId: String) {
        val oldestId = repository.getOldestFavoriteGameId()
        if (oldestId != null) repository.removeFavorite(oldestId)
        repository.addFavorite(gameId)
    }

    suspend fun remove(gameId: String) = repository.removeFavorite(gameId)

    companion object {
        const val MAX_FAVORITES = 3
    }
}

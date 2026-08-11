package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ProfileRepository
import com.rogue.scorequest.domain.model.BoardGame
import kotlinx.coroutines.flow.Flow

class GetFavoriteGamesUseCase(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<List<BoardGame>> = repository.getFavoriteGames()
}

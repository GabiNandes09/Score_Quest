package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import kotlinx.coroutines.flow.Flow

class GetGamesUseCase(
    private val repository: BoardGameRepository
) {
    operator fun invoke(): Flow<List<GameWithLibraryInfo>> = repository.getGames()
}

package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import kotlinx.coroutines.flow.Flow

class GetGameDetailUseCase(
    private val repository: BoardGameRepository
) {
    operator fun invoke(gameId: String): Flow<GameWithLibraryInfo?> = repository.getGame(gameId)
}

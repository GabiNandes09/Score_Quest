package com.rogue.scorequest.domain.usecase

import androidx.paging.PagingData
import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.SessionWithDetails
import kotlinx.coroutines.flow.Flow

class GetSessionsPagedUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(): Flow<PagingData<SessionWithDetails>> = repository.getSessionsPaged()
}

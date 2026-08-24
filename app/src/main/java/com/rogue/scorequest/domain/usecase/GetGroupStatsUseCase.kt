package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.GroupStats
import kotlinx.coroutines.flow.Flow

class GetGroupStatsUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(groupId: String): Flow<GroupStats> = repository.getGroupStats(groupId)
}

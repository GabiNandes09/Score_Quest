package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerGroupRepository
import com.rogue.scorequest.domain.model.PlayerGroup
import kotlinx.coroutines.flow.Flow

class GetPlayerGroupUseCase(
    private val repository: PlayerGroupRepository
) {
    operator fun invoke(groupId: String): Flow<PlayerGroup?> = repository.getGroup(groupId)
}

package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerGroupRepository
import com.rogue.scorequest.domain.model.PlayerGroup
import kotlinx.coroutines.flow.Flow

class GetPlayerGroupsUseCase(
    private val repository: PlayerGroupRepository
) {
    operator fun invoke(): Flow<List<PlayerGroup>> = repository.getGroups()
}

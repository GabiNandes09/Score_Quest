package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerGroupRepository
import com.rogue.scorequest.domain.model.PlayerGroup

class FindGroupWithExactMembersUseCase(
    private val repository: PlayerGroupRepository
) {
    suspend operator fun invoke(memberIds: Set<String>): PlayerGroup? =
        repository.getGroupsOnce().find { it.memberIds.toSet() == memberIds }
}

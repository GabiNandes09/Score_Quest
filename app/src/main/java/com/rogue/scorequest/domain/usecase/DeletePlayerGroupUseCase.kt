package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerGroupRepository

class DeletePlayerGroupUseCase(
    private val repository: PlayerGroupRepository
) {
    suspend operator fun invoke(groupId: String) {
        repository.deleteGroup(groupId, System.currentTimeMillis())
    }
}

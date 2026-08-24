package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerGroupRepository
import com.rogue.scorequest.domain.model.PlayerGroup
import java.time.LocalDateTime

class UpdatePlayerGroupUseCase(
    private val repository: PlayerGroupRepository
) {
    suspend operator fun invoke(group: PlayerGroup, newName: String, newPhotoPath: String?, newMemberIds: List<String>) {
        repository.updateGroup(
            group.copy(
                name = newName,
                photoPath = newPhotoPath,
                memberIds = newMemberIds,
                updatedAt = LocalDateTime.now()
            )
        )
    }
}

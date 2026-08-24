package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.PlayerGroupRepository
import com.rogue.scorequest.domain.model.PlayerGroup
import java.time.LocalDateTime
import java.util.UUID

class CreatePlayerGroupUseCase(
    private val repository: PlayerGroupRepository
) {
    suspend operator fun invoke(name: String, photoPath: String?, memberIds: List<String>): PlayerGroup {
        val now = LocalDateTime.now()
        val group = PlayerGroup(
            id = UUID.randomUUID().toString(),
            name = name,
            photoPath = photoPath,
            memberIds = memberIds,
            createdAt = now,
            updatedAt = now
        )
        repository.createGroup(group)
        return group
    }
}

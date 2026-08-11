package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ProfileRepository
import com.rogue.scorequest.domain.model.UserProfile
import java.time.LocalDateTime

class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(displayName: String, bio: String?, avatarUri: String?) {
        repository.saveProfile(
            UserProfile(
                displayName = displayName,
                bio = bio,
                avatarUri = avatarUri,
                updatedAt = LocalDateTime.now()
            )
        )
    }
}

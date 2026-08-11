package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ProfileRepository
import com.rogue.scorequest.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

class GetProfileUseCase(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<UserProfile?> = repository.getProfile()
}

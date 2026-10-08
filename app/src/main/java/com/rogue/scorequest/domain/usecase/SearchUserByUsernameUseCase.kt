package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudPublicProfile
import com.rogue.scorequest.data.repository.FriendRepository

class SearchUserByUsernameUseCase(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(username: String): CloudPublicProfile? =
        friendRepository.findUserByUsername(username)
}

package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudFriendRequest
import com.rogue.scorequest.data.repository.FriendRepository

class RespondToFriendRequestUseCase(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(request: CloudFriendRequest, accept: Boolean) {
        friendRepository.respondToRequest(request.id, accept)
        if (accept) {
            friendRepository.addFriendPair(request)
        }
    }
}

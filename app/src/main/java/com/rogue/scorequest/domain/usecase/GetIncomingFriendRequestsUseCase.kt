package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudFriendRequest
import com.rogue.scorequest.data.repository.FriendRepository

class GetIncomingFriendRequestsUseCase(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(uid: String): List<CloudFriendRequest> =
        friendRepository.getIncomingRequests(uid)
}

package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudFriend
import com.rogue.scorequest.data.repository.FriendRepository

class GetFriendsUseCase(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(uid: String): List<CloudFriend> = friendRepository.getFriends(uid)
}

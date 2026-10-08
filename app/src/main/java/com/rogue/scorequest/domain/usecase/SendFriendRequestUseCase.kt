package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudFriendRequest
import com.rogue.scorequest.data.cloud.CloudPublicProfile
import com.rogue.scorequest.data.repository.FriendRepository
import com.rogue.scorequest.domain.model.AuthUser
import com.rogue.scorequest.domain.model.FriendRequestResult
import com.rogue.scorequest.domain.model.FriendRequestStatus

class SendFriendRequestUseCase(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(me: AuthUser, myUsername: String, target: CloudPublicProfile): FriendRequestResult {
        if (target.uid == me.uid) return FriendRequestResult.CANNOT_ADD_SELF

        val alreadyFriends = friendRepository.getFriends(me.uid).any { it.uid == target.uid }
        if (alreadyFriends) return FriendRequestResult.ALREADY_FRIENDS

        val pendingRequest = friendRepository.findPendingRequestBetween(me.uid, target.uid)
        if (pendingRequest != null) return FriendRequestResult.ALREADY_PENDING

        friendRepository.sendFriendRequest(
            CloudFriendRequest(
                fromUid = me.uid,
                fromUsername = myUsername,
                fromDisplayName = me.displayName,
                fromPhotoUrl = me.photoUrl,
                toUid = target.uid,
                toUsername = target.username,
                toDisplayName = target.displayName,
                toPhotoUrl = target.photoUrl,
                status = FriendRequestStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
        )
        return FriendRequestResult.SENT
    }
}

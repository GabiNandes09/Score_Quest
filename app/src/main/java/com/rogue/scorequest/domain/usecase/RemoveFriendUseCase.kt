package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.FriendRepository

class RemoveFriendUseCase(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(uid: String, friendUid: String) = friendRepository.removeFriendPair(uid, friendUid)
}

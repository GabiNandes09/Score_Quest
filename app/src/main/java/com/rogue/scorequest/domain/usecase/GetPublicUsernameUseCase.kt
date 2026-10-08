package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.CloudBackupRepository

class GetPublicUsernameUseCase(
    private val cloudBackupRepository: CloudBackupRepository
) {
    suspend operator fun invoke(uid: String): String? =
        cloudBackupRepository.downloadPublicProfile(uid)?.username
}

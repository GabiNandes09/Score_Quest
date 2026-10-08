package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository,
    private val syncAccountData: SyncAccountDataUseCase
) {
    suspend operator fun invoke(idToken: String) {
        val user = authRepository.signInWithGoogleIdToken(idToken)
        syncAccountData(user.uid)
    }
}

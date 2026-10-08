package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.AuthRepository

class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val clearLocalData: ClearLocalDataUseCase
) {
    suspend operator fun invoke() {
        authRepository.signOut()
        clearLocalData()
    }
}

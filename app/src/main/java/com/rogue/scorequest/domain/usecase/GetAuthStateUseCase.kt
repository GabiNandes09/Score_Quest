package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.AuthRepository
import com.rogue.scorequest.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

class GetAuthStateUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthUser?> = authRepository.authStateChanges()
}

package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.local.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Apaga todos os dados locais (Room) — usado no logout, pra não vazar dado de um usuário pro próximo que usar o aparelho. */
class ClearLocalDataUseCase(
    private val appDatabase: AppDatabase
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        appDatabase.clearAllTables()
    }
}

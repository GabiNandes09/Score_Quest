package com.rogue.scorequest.data.repository

import com.rogue.scorequest.data.local.dao.ActiveTimerDao
import com.rogue.scorequest.data.local.entity.toDomain
import com.rogue.scorequest.data.local.entity.toEntity
import com.rogue.scorequest.domain.model.ActiveTimer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ActiveTimerRepository(
    private val activeTimerDao: ActiveTimerDao
) {

    fun observe(): Flow<ActiveTimer?> = activeTimerDao.observe().map { it?.toDomain() }

    suspend fun getOnce(): ActiveTimer? = activeTimerDao.getOnce()?.toDomain()

    suspend fun save(timer: ActiveTimer) = activeTimerDao.upsert(timer.toEntity())

    suspend fun clear() = activeTimerDao.clear()

    // Chamado ao salvar uma partida que veio de um cronômetro finalizado — só limpa se o
    // timer ativo ainda for do mesmo jogo (defesa contra o caso raro de trocar de jogo no
    // wizard depois de finalizar o cronômetro de outro).
    suspend fun clearIfGameMatches(gameId: String) {
        val current = activeTimerDao.getOnce()
        if (current?.gameId == gameId) activeTimerDao.clear()
    }
}

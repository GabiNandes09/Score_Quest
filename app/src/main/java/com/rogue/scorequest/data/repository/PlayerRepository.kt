package com.rogue.scorequest.data.repository

import com.rogue.scorequest.data.local.dao.PlayerDao
import com.rogue.scorequest.data.local.entity.toDomain
import com.rogue.scorequest.data.local.entity.toEntity
import com.rogue.scorequest.domain.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerRepository(
    private val playerDao: PlayerDao
) {

    fun getPlayers(): Flow<List<Player>> =
        playerDao.getAll().map { list -> list.map { it.toDomain() } }

    fun getPlayer(id: String): Flow<Player?> =
        playerDao.getById(id).map { it?.toDomain() }

    suspend fun insertPlayer(player: Player) = playerDao.insert(player.toEntity())

    suspend fun updatePlayer(player: Player) = playerDao.update(player.toEntity())

    suspend fun deletePlayer(id: String, deletedAt: Long) = playerDao.softDelete(id, deletedAt)

    suspend fun hasHistory(playerId: String): Boolean = playerDao.hasHistory(playerId)
}

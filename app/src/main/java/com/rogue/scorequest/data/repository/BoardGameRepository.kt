package com.rogue.scorequest.data.repository

import com.rogue.scorequest.data.local.dao.BoardGameDao
import com.rogue.scorequest.data.local.dao.UserLibraryEntryDao
import com.rogue.scorequest.data.local.entity.toDomain
import com.rogue.scorequest.data.local.entity.toEntity
import com.rogue.scorequest.domain.model.BoardGame
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.domain.model.UserLibraryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BoardGameRepository(
    private val boardGameDao: BoardGameDao,
    private val userLibraryEntryDao: UserLibraryEntryDao
) {

    fun getGames(): Flow<List<GameWithLibraryInfo>> =
        boardGameDao.getAllWithLibraryInfo().map { list -> list.map { it.toDomain() } }

    fun getGame(gameId: String): Flow<GameWithLibraryInfo?> =
        boardGameDao.getByIdWithLibraryInfo(gameId).map { it?.toDomain() }

    suspend fun findGameOnce(gameId: String): BoardGame? =
        boardGameDao.findById(gameId)?.toDomain()

    suspend fun insertGame(game: BoardGame) = boardGameDao.insert(game.toEntity())

    suspend fun updateGame(game: BoardGame) = boardGameDao.update(game.toEntity())

    suspend fun deleteGame(id: String, deletedAt: Long) = boardGameDao.softDelete(id, deletedAt)

    suspend fun getLibraryEntryOnce(gameId: String): UserLibraryEntry? =
        userLibraryEntryDao.findByGameId(gameId)?.toDomain()

    suspend fun upsertLibraryEntry(entry: UserLibraryEntry) =
        userLibraryEntryDao.upsert(entry.toEntity())
}

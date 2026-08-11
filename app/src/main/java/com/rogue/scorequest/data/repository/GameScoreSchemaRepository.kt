package com.rogue.scorequest.data.repository

import com.rogue.scorequest.data.local.dao.GameScoreSchemaDao
import com.rogue.scorequest.data.local.entity.toDomain
import com.rogue.scorequest.data.local.entity.toEntity
import com.rogue.scorequest.domain.model.GameScoreSchema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameScoreSchemaRepository(
    private val gameScoreSchemaDao: GameScoreSchemaDao
) {

    fun getByGameId(gameId: String): Flow<GameScoreSchema?> =
        gameScoreSchemaDao.getByGameId(gameId).map { it?.toDomain() }

    suspend fun findByGameIdOnce(gameId: String): GameScoreSchema? =
        gameScoreSchemaDao.findByGameId(gameId)?.toDomain()

    suspend fun save(schema: GameScoreSchema) = gameScoreSchemaDao.upsert(schema.toEntity())

    fun getAllCompositeSchemas(): Flow<List<GameScoreSchema>> =
        gameScoreSchemaDao.getAllCompositeSchemas().map { list -> list.map { it.toDomain() } }
}

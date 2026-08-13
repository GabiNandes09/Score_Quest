package com.rogue.scorequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rogue.scorequest.data.local.entity.GameScoreSchemaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameScoreSchemaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schema: GameScoreSchemaEntity)

    @Query("SELECT * FROM game_score_schema WHERE game_id = :gameId AND deleted_at IS NULL")
    fun getByGameId(gameId: String): Flow<GameScoreSchemaEntity?>

    @Query("SELECT * FROM game_score_schema WHERE game_id = :gameId AND deleted_at IS NULL")
    suspend fun findByGameId(gameId: String): GameScoreSchemaEntity?

    @Query("SELECT * FROM game_score_schema WHERE deleted_at IS NULL AND type = 'COMPOSITE'")
    fun getAllCompositeSchemas(): Flow<List<GameScoreSchemaEntity>>

    @Query("SELECT * FROM game_score_schema WHERE deleted_at IS NULL")
    suspend fun getAllOnce(): List<GameScoreSchemaEntity>
}

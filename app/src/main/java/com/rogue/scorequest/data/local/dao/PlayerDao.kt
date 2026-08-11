package com.rogue.scorequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rogue.scorequest.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert
    suspend fun insert(player: PlayerEntity)

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("UPDATE player SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("SELECT * FROM player WHERE deleted_at IS NULL ORDER BY nickname ASC")
    fun getAll(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM player WHERE id = :id AND deleted_at IS NULL")
    fun getById(id: String): Flow<PlayerEntity?>

    @Query("SELECT * FROM player WHERE id = :id AND deleted_at IS NULL")
    suspend fun findById(id: String): PlayerEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM score_entry WHERE player_id = :playerId AND deleted_at IS NULL)")
    suspend fun hasHistory(playerId: String): Boolean
}

package com.rogue.scorequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rogue.scorequest.data.local.entity.BoardGameEntity
import com.rogue.scorequest.data.local.entity.GameWithLibraryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardGameDao {

    @Insert
    suspend fun insert(game: BoardGameEntity)

    @Update
    suspend fun update(game: BoardGameEntity)

    @Query("UPDATE board_game SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("SELECT * FROM board_game WHERE id = :id AND deleted_at IS NULL")
    suspend fun findById(id: String): BoardGameEntity?

    @Transaction
    @Query("SELECT * FROM board_game WHERE deleted_at IS NULL ORDER BY name ASC")
    fun getAllWithLibraryInfo(): Flow<List<GameWithLibraryEntryEntity>>

    @Transaction
    @Query("SELECT * FROM board_game WHERE id = :id AND deleted_at IS NULL")
    fun getByIdWithLibraryInfo(id: String): Flow<GameWithLibraryEntryEntity?>
}

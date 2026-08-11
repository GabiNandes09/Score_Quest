package com.rogue.scorequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rogue.scorequest.data.local.entity.BoardGameEntity
import com.rogue.scorequest.data.local.entity.FavoriteGameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteGameDao {

    @Insert
    suspend fun insert(favorite: FavoriteGameEntity)

    @Query("DELETE FROM favorite_game WHERE game_id = :gameId")
    suspend fun deleteByGameId(gameId: String)

    @Query("SELECT COUNT(*) FROM favorite_game")
    suspend fun getCount(): Int

    @Query("SELECT * FROM favorite_game ORDER BY created_at ASC LIMIT 1")
    suspend fun getOldest(): FavoriteGameEntity?

    @Query(
        """
        SELECT bg.* FROM favorite_game fg
        INNER JOIN board_game bg ON bg.id = fg.game_id
        ORDER BY fg.position ASC
        """
    )
    fun getFavoriteGames(): Flow<List<BoardGameEntity>>
}

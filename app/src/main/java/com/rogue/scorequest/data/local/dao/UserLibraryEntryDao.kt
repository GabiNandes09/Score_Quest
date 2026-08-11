package com.rogue.scorequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rogue.scorequest.data.local.entity.UserLibraryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLibraryEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: UserLibraryEntryEntity)

    @Query("SELECT * FROM user_library_entry WHERE game_id = :gameId AND deleted_at IS NULL")
    fun getByGameId(gameId: String): Flow<UserLibraryEntryEntity?>

    @Query("SELECT * FROM user_library_entry WHERE game_id = :gameId AND deleted_at IS NULL")
    suspend fun findByGameId(gameId: String): UserLibraryEntryEntity?

    @Query("SELECT * FROM user_library_entry WHERE deleted_at IS NULL")
    fun getAll(): Flow<List<UserLibraryEntryEntity>>
}

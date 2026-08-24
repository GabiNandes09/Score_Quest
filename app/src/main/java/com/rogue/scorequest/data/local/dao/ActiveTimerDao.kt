package com.rogue.scorequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rogue.scorequest.data.local.entity.ActiveTimerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveTimerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ActiveTimerEntity)

    @Query("SELECT * FROM active_timer LIMIT 1")
    fun observe(): Flow<ActiveTimerEntity?>

    @Query("SELECT * FROM active_timer LIMIT 1")
    suspend fun getOnce(): ActiveTimerEntity?

    @Query("DELETE FROM active_timer")
    suspend fun clear()
}

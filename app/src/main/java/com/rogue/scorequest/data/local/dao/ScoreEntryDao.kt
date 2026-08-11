package com.rogue.scorequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.rogue.scorequest.data.local.entity.ScoreEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreEntryDao {

    @Insert
    suspend fun insertAll(scores: List<ScoreEntryEntity>)

    @Query("DELETE FROM score_entry WHERE session_id = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)

    @Transaction
    suspend fun replaceScoresForSession(sessionId: String, scores: List<ScoreEntryEntity>) {
        deleteBySessionId(sessionId)
        insertAll(scores)
    }

    @Query("SELECT * FROM score_entry WHERE session_id = :sessionId AND deleted_at IS NULL")
    fun getBySessionId(sessionId: String): Flow<List<ScoreEntryEntity>>

    @Query("SELECT * FROM score_entry WHERE session_id = :sessionId AND deleted_at IS NULL")
    suspend fun getBySessionIdOnce(sessionId: String): List<ScoreEntryEntity>

    @Query(
        """
        SELECT MAX(se.total_score) FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        WHERE gs.game_id = :gameId AND gs.deleted_at IS NULL AND se.deleted_at IS NULL
        """
    )
    fun getMaxScoreForGame(gameId: String): Flow<Int?>
}

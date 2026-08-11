package com.rogue.scorequest.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rogue.scorequest.data.local.entity.GameSessionEntity
import com.rogue.scorequest.data.local.entity.SessionWithScoresEntity
import com.rogue.scorequest.domain.model.GameLastPlayed
import com.rogue.scorequest.domain.model.GamePlayCount
import com.rogue.scorequest.domain.model.MonthlyPlaytime
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    @Insert
    suspend fun insert(session: GameSessionEntity)

    @Update
    suspend fun update(session: GameSessionEntity)

    @Query("UPDATE game_session SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("SELECT * FROM game_session WHERE id = :id AND deleted_at IS NULL")
    suspend fun findById(id: String): GameSessionEntity?

    @Transaction
    @Query("SELECT * FROM game_session WHERE id = :id AND deleted_at IS NULL")
    fun getSessionWithScores(id: String): Flow<SessionWithScoresEntity?>

    @Transaction
    @Query("SELECT * FROM game_session WHERE game_id = :gameId AND deleted_at IS NULL ORDER BY date DESC, created_at DESC")
    fun getByGameIdWithScores(gameId: String): Flow<List<SessionWithScoresEntity>>

    @Transaction
    @Query("SELECT * FROM game_session WHERE deleted_at IS NULL ORDER BY date DESC, created_at DESC LIMIT :limit")
    fun getRecentSessionsWithScores(limit: Int): Flow<List<SessionWithScoresEntity>>

    @Transaction
    @Query("SELECT * FROM game_session WHERE deleted_at IS NULL ORDER BY date DESC, created_at DESC")
    fun getSessionsWithScoresPaged(): PagingSource<Int, SessionWithScoresEntity>

    @Query("SELECT date FROM game_session WHERE deleted_at IS NULL ORDER BY date DESC")
    fun getAllSessionDates(): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM game_session WHERE deleted_at IS NULL")
    fun getSessionCount(): Flow<Int>

    @Query(
        """
        SELECT gs.game_id AS gameId, bg.name AS gameName, COUNT(*) AS playCount
        FROM game_session gs
        INNER JOIN board_game bg ON bg.id = gs.game_id
        WHERE gs.deleted_at IS NULL
        GROUP BY gs.game_id
        ORDER BY playCount DESC
        LIMIT :limit
        """
    )
    fun getTopPlayedGames(limit: Int): Flow<List<GamePlayCount>>

    @Query("SELECT SUM(duration_minutes) FROM game_session WHERE deleted_at IS NULL")
    fun getTotalDurationMinutes(): Flow<Int?>

    @Query(
        """
        SELECT strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) AS yearMonth, SUM(duration_minutes) AS totalMinutes
        FROM game_session
        WHERE deleted_at IS NULL AND date >= :sinceEpoch
        GROUP BY yearMonth
        """
    )
    fun getMonthlyPlaytimeSince(sinceEpoch: Long): Flow<List<MonthlyPlaytime>>

    @Query("SELECT COUNT(*) FROM game_session WHERE game_id = :gameId AND deleted_at IS NULL")
    fun getTimesPlayed(gameId: String): Flow<Int>

    @Query("SELECT AVG(duration_minutes) FROM game_session WHERE game_id = :gameId AND deleted_at IS NULL")
    fun getAvgDurationForGame(gameId: String): Flow<Double?>

    @Query(
        """
        SELECT game_id AS gameId, MAX(date) AS lastPlayedDate
        FROM game_session
        WHERE deleted_at IS NULL
        GROUP BY game_id
        """
    )
    fun getLastPlayedDates(): Flow<List<GameLastPlayed>>
}

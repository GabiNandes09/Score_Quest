package com.rogue.scorequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.rogue.scorequest.data.local.entity.ScoreEntryEntity
import com.rogue.scorequest.domain.model.GamePlayCount
import com.rogue.scorequest.domain.model.GameScoreRecord
import com.rogue.scorequest.domain.model.PlayerCoreCounts
import com.rogue.scorequest.domain.model.PlayerPlayCount
import com.rogue.scorequest.domain.model.PlayerSessionResult
import com.rogue.scorequest.domain.model.PlayerWinCount
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

    @Query(
        """
        SELECT
            COUNT(*) AS gamesPlayed,
            SUM(CASE WHEN se.is_winner = 1 THEN 1 ELSE 0 END) AS wins,
            SUM(CASE WHEN se.is_winner IS NOT NULL THEN 1 ELSE 0 END) AS decidedGames
        FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        WHERE se.player_id = :playerId AND se.deleted_at IS NULL AND gs.deleted_at IS NULL
        """
    )
    fun getPlayerCoreCounts(playerId: String): Flow<PlayerCoreCounts>

    @Query(
        """
        SELECT SUM(gs.duration_minutes) FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        WHERE se.player_id = :playerId AND se.deleted_at IS NULL AND gs.deleted_at IS NULL
        """
    )
    fun getPlayerTotalMinutes(playerId: String): Flow<Int?>

    @Query(
        """
        SELECT gs.date AS date, se.is_winner AS isWinner
        FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        WHERE se.player_id = :playerId AND se.deleted_at IS NULL AND gs.deleted_at IS NULL
        ORDER BY gs.date ASC, gs.created_at ASC
        """
    )
    fun getPlayerSessionResults(playerId: String): Flow<List<PlayerSessionResult>>

    @Query(
        """
        SELECT gs.game_id AS gameId, bg.name AS gameName, COUNT(*) AS playCount
        FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        INNER JOIN board_game bg ON bg.id = gs.game_id
        WHERE se.player_id = :playerId AND se.deleted_at IS NULL AND gs.deleted_at IS NULL
        GROUP BY gs.game_id
        ORDER BY playCount DESC
        LIMIT :limit
        """
    )
    fun getPlayerTopGames(playerId: String, limit: Int): Flow<List<GamePlayCount>>

    @Query(
        """
        SELECT gs.game_id AS gameId, bg.name AS gameName, COUNT(*) AS playCount
        FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        INNER JOIN board_game bg ON bg.id = gs.game_id
        WHERE se.player_id = :playerId AND se.is_winner = 1 AND se.deleted_at IS NULL AND gs.deleted_at IS NULL
        GROUP BY gs.game_id
        ORDER BY playCount DESC
        LIMIT 1
        """
    )
    fun getPlayerFavoriteGame(playerId: String): Flow<GamePlayCount?>

    @Query(
        """
        SELECT se.player_id AS playerId, p.nickname AS playerName, COUNT(*) AS wins
        FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        INNER JOIN player p ON p.id = se.player_id
        WHERE se.is_winner = 1 AND se.deleted_at IS NULL AND gs.deleted_at IS NULL AND p.deleted_at IS NULL
        GROUP BY se.player_id
        ORDER BY wins DESC
        LIMIT :limit
        """
    )
    fun getTopPlayersByWins(limit: Int): Flow<List<PlayerWinCount>>

    @Query(
        """
        SELECT se.player_id AS playerId, p.nickname AS playerName, COUNT(*) AS wins
        FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        INNER JOIN player p ON p.id = se.player_id
        WHERE gs.group_id = :groupId AND se.is_winner = 1 AND se.deleted_at IS NULL AND gs.deleted_at IS NULL AND p.deleted_at IS NULL
        GROUP BY se.player_id
        ORDER BY wins DESC
        """
    )
    fun getGroupMemberWins(groupId: String): Flow<List<PlayerWinCount>>

    @Query(
        """
        SELECT se.player_id AS playerId, p.nickname AS playerName, COUNT(*) AS playCount
        FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        INNER JOIN player p ON p.id = se.player_id
        WHERE gs.game_id = :gameId AND se.deleted_at IS NULL AND gs.deleted_at IS NULL AND p.deleted_at IS NULL
        GROUP BY se.player_id
        ORDER BY playCount DESC
        LIMIT :limit
        """
    )
    fun getTopPlayersByPlaysForGame(gameId: String, limit: Int): Flow<List<PlayerPlayCount>>

    @Query(
        """
        SELECT se.player_id AS playerId, p.nickname AS playerName, COUNT(*) AS wins
        FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        INNER JOIN player p ON p.id = se.player_id
        WHERE gs.game_id = :gameId AND se.is_winner = 1 AND se.deleted_at IS NULL AND gs.deleted_at IS NULL AND p.deleted_at IS NULL
        GROUP BY se.player_id
        ORDER BY wins DESC
        LIMIT :limit
        """
    )
    fun getTopPlayersByWinsForGame(gameId: String, limit: Int): Flow<List<PlayerWinCount>>

    @Query(
        """
        SELECT gs.id AS sessionId, se.player_id AS playerId, p.nickname AS playerName, se.total_score AS score, gs.date AS date
        FROM score_entry se
        INNER JOIN game_session gs ON gs.id = se.session_id
        INNER JOIN player p ON p.id = se.player_id
        WHERE gs.game_id = :gameId AND se.total_score IS NOT NULL AND se.deleted_at IS NULL AND gs.deleted_at IS NULL AND p.deleted_at IS NULL
        ORDER BY se.total_score DESC
        LIMIT :limit
        """
    )
    fun getTopScoresForGame(gameId: String, limit: Int): Flow<List<GameScoreRecord>>
}

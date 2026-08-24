package com.rogue.scorequest.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.rogue.scorequest.data.local.dao.GameSessionDao
import com.rogue.scorequest.data.local.dao.ScoreEntryDao
import com.rogue.scorequest.data.local.dao.UserLibraryEntryDao
import com.rogue.scorequest.data.local.entity.UserLibraryEntryEntity
import com.rogue.scorequest.data.local.entity.toDomain
import com.rogue.scorequest.data.local.entity.toEntity
import com.rogue.scorequest.domain.model.DayActivity
import com.rogue.scorequest.domain.model.GamePlayCount
import com.rogue.scorequest.domain.model.GameSession
import com.rogue.scorequest.domain.model.GameStats
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.MonthSessionCount
import com.rogue.scorequest.domain.model.PlayerStats
import com.rogue.scorequest.domain.model.ScoreEntry
import com.rogue.scorequest.domain.model.SessionWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

private const val PLAYER_TOP_GAMES_LIMIT = 5

class GameSessionRepository(
    private val gameSessionDao: GameSessionDao,
    private val scoreEntryDao: ScoreEntryDao,
    private val userLibraryEntryDao: UserLibraryEntryDao
) {

    suspend fun saveSession(session: GameSession, scores: List<ScoreEntry>) {
        gameSessionDao.insert(session.toEntity())
        scoreEntryDao.insertAll(scores.map { it.toEntity() })
        ensureLibraryEntryPlayed(session.gameId)
    }

    suspend fun updateSession(session: GameSession, scores: List<ScoreEntry>) {
        gameSessionDao.update(session.toEntity())
        scoreEntryDao.replaceScoresForSession(session.id, scores.map { it.toEntity() })
    }

    suspend fun deleteSession(id: String, deletedAt: Long) = gameSessionDao.softDelete(id, deletedAt)

    fun getSessionDetail(sessionId: String): Flow<SessionWithDetails?> =
        gameSessionDao.getSessionWithScores(sessionId).map { it?.toDomain() }

    fun getSessionsForGame(gameId: String): Flow<List<SessionWithDetails>> =
        gameSessionDao.getByGameIdWithScores(gameId).map { list -> list.map { it.toDomain() } }

    fun getRecentSessions(limit: Int): Flow<List<SessionWithDetails>> =
        gameSessionDao.getRecentSessionsWithScores(limit).map { list -> list.map { it.toDomain() } }

    fun getSessionsPaged(): Flow<PagingData<SessionWithDetails>> =
        Pager(PagingConfig(pageSize = 20)) { gameSessionDao.getSessionsWithScoresPaged() }
            .flow
            .map { pagingData -> pagingData.map { it.toDomain() } }

    fun getTopPlayedGames(limit: Int): Flow<List<GamePlayCount>> = gameSessionDao.getTopPlayedGames(limit)

    fun getTotalDurationMinutes(): Flow<Int?> = gameSessionDao.getTotalDurationMinutes()

    fun getDurationMinutesSince(sinceEpoch: Long): Flow<Int?> = gameSessionDao.getDurationMinutesSince(sinceEpoch)

    fun getActivityByDaySince(sinceEpoch: Long): Flow<List<DayActivity>> =
        gameSessionDao.getActivityByDaySince(sinceEpoch)

    fun getSessionCountByYear(year: String): Flow<List<MonthSessionCount>> =
        gameSessionDao.getSessionCountByYear(year)

    fun getAllSessionDurations(): Flow<List<Int>> = gameSessionDao.getAllSessionDurations()

    fun getAllSessionDates(): Flow<List<Long>> = gameSessionDao.getAllSessionDates()

    fun getSessionCount(): Flow<Int> = gameSessionDao.getSessionCount()

    fun getLastPlayedDates(): Flow<Map<String, Long>> =
        gameSessionDao.getLastPlayedDates().map { rows -> rows.associate { it.gameId to it.lastPlayedDate } }

    fun getGameStats(gameId: String): Flow<GameStats> =
        combine(
            gameSessionDao.getTimesPlayed(gameId),
            gameSessionDao.getAvgDurationForGame(gameId),
            scoreEntryDao.getMaxScoreForGame(gameId)
        ) { timesPlayed, avgDuration, highScore ->
            GameStats(
                timesPlayed = timesPlayed,
                avgDurationMinutes = avgDuration?.roundToInt() ?: 0,
                highScore = highScore
            )
        }

    fun getPlayerStats(playerId: String): Flow<PlayerStats> =
        combine(
            scoreEntryDao.getPlayerCoreCounts(playerId),
            scoreEntryDao.getPlayerTotalMinutes(playerId),
            scoreEntryDao.getPlayerSessionResults(playerId),
            scoreEntryDao.getPlayerTopGames(playerId, PLAYER_TOP_GAMES_LIMIT),
            scoreEntryDao.getPlayerFavoriteGame(playerId)
        ) { coreCounts, totalMinutes, sessionResults, topGames, favoriteGame ->
            val (currentStreak, bestStreak) = calculateWinStreaks(sessionResults.map { it.isWinner })
            PlayerStats(
                gamesPlayed = coreCounts.gamesPlayed,
                wins = coreCounts.wins,
                decidedGames = coreCounts.decidedGames,
                winRate = if (coreCounts.decidedGames > 0) coreCounts.wins.toDouble() / coreCounts.decidedGames else null,
                totalMinutes = totalMinutes ?: 0,
                currentWinStreak = currentStreak,
                bestWinStreak = bestStreak,
                topGames = topGames,
                favoriteGame = favoriteGame
            )
        }

    // Sequência de vitórias: quebra em qualquer resultado que não seja vitória (derrota OU
    // partida sem vencedor definido, ex. cooperativa) — diferente da streak diária da Home,
    // não tem conceito de "expirar", é só a maior sequência consecutiva na lista ordenada.
    private fun calculateWinStreaks(results: List<Boolean?>): Pair<Int, Int> {
        var best = 0
        var running = 0
        for (isWinner in results) {
            if (isWinner == true) {
                running++
                if (running > best) best = running
            } else {
                running = 0
            }
        }
        return running to best
    }

    private suspend fun ensureLibraryEntryPlayed(gameId: String) {
        val now = System.currentTimeMillis()
        val existing = userLibraryEntryDao.findByGameId(gameId)
        if (existing == null) {
            userLibraryEntryDao.upsert(
                UserLibraryEntryEntity(
                    gameId = gameId,
                    status = LibraryStatus.DONT_HAVE,
                    played = true,
                    lentTo = null,
                    rating = null,
                    createdAt = now,
                    updatedAt = now,
                    deletedAt = null
                )
            )
        } else if (!existing.played) {
            userLibraryEntryDao.upsert(existing.copy(played = true, updatedAt = now))
        }
    }
}

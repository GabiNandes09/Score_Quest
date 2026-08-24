package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.HomeStats
import com.rogue.scorequest.utils.toEpochMillis
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeStatsUseCase(
    private val sessionRepository: GameSessionRepository,
    private val getStreak: GetStreakUseCase
) {
    operator fun invoke(): Flow<HomeStats> {
        val startOfWeekEpoch = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay().toEpochMillis()

        return combine(
            getStreak(),
            sessionRepository.getTopPlayedGames(TOP_GAMES_LIMIT),
            sessionRepository.getTopPlayersByWins(TOP_PLAYERS_LIMIT),
            sessionRepository.getDurationMinutesSince(startOfWeekEpoch),
            sessionRepository.getTotalDurationMinutes()
        ) { streak, topGames, topPlayersByWins, weekMinutes, totalMinutes ->
            HomeStats(
                streakDays = streak.days,
                isStreakActive = streak.isActive,
                topGames = topGames,
                topPlayersByWins = topPlayersByWins,
                weekMinutes = weekMinutes ?: 0,
                totalMinutes = totalMinutes ?: 0
            )
        }
    }

    companion object {
        private const val TOP_GAMES_LIMIT = 5
        private const val TOP_PLAYERS_LIMIT = 5
    }
}

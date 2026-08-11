package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.HomeStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeStatsUseCase(
    private val sessionRepository: GameSessionRepository,
    private val getStreak: GetStreakUseCase
) {
    operator fun invoke(): Flow<HomeStats> {
        return combine(
            getStreak(),
            sessionRepository.getTopPlayedGames(TOP_GAMES_LIMIT),
            sessionRepository.getTotalDurationMinutes()
        ) { streak, topGames, totalMinutes ->
            HomeStats(
                streakDays = streak.days,
                isStreakActive = streak.isActive,
                topGames = topGames,
                totalMinutes = totalMinutes ?: 0
            )
        }
    }

    companion object {
        private const val TOP_GAMES_LIMIT = 3
    }
}

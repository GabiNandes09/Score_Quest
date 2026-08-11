package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.StreakInfo
import com.rogue.scorequest.utils.toLocalDateTime
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetStreakUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(): Flow<StreakInfo> =
        repository.getAllSessionDates().map { epochList ->
            val dates = epochList
                .map { it.toLocalDateTime().toLocalDate() }
                .distinct()
                .sortedDescending()

            if (dates.isEmpty()) return@map StreakInfo(days = 0, isActive = false)

            val today = LocalDate.now()
            val mostRecent = dates.first()
            val gapFromToday = ChronoUnit.DAYS.between(mostRecent, today)

            if (gapFromToday <= 1) {
                var streak = 1
                var cursor = mostRecent
                for (i in 1 until dates.size) {
                    val expected = cursor.minusDays(1)
                    if (dates[i] == expected) {
                        streak++
                        cursor = expected
                    } else {
                        break
                    }
                }
                StreakInfo(days = streak, isActive = true)
            } else {
                StreakInfo(days = gapFromToday.toInt(), isActive = false)
            }
        }
}

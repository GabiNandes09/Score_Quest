package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.StreakInfo
import com.rogue.scorequest.utils.toLocalDateTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetStreakUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(): Flow<StreakInfo> =
        repository.getAllSessionDates().map { epochList ->
            // Agrupado por semana (segunda-feira da semana de cada partida, mesma
            // convenção de início de semana usada em GetHomeStatsUseCase.weekMinutes) —
            // a lógica de sequência consecutiva é igual à versão por dia, só na
            // granularidade de semana em vez de dia.
            val weeks = epochList
                .map { it.toLocalDateTime().toLocalDate().with(DayOfWeek.MONDAY) }
                .distinct()
                .sortedDescending()

            if (weeks.isEmpty()) return@map StreakInfo(weeks = 0, isActive = false)

            val currentWeek = LocalDate.now().with(DayOfWeek.MONDAY)
            val mostRecentWeek = weeks.first()
            val gapFromCurrentWeek = ChronoUnit.WEEKS.between(mostRecentWeek, currentWeek)

            if (gapFromCurrentWeek <= 1) {
                var streak = 1
                var cursor = mostRecentWeek
                for (i in 1 until weeks.size) {
                    val expected = cursor.minusWeeks(1)
                    if (weeks[i] == expected) {
                        streak++
                        cursor = expected
                    } else {
                        break
                    }
                }
                StreakInfo(weeks = streak, isActive = true)
            } else {
                StreakInfo(weeks = gapFromCurrentWeek.toInt(), isActive = false)
            }
        }
}

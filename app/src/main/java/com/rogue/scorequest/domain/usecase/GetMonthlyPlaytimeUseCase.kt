package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.MonthlyPlaytime
import com.rogue.scorequest.utils.toEpochMillis
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val MONTHS_WINDOW = 12

class GetMonthlyPlaytimeUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(): Flow<List<MonthlyPlaytime>> {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths((MONTHS_WINDOW - 1).toLong())
        val sinceEpoch = startMonth.atDay(1).atStartOfDay().toEpochMillis()

        return repository.getMonthlyPlaytimeSince(sinceEpoch).map { rows ->
            val byMonth = rows.associateBy { it.yearMonth }
            (0 until MONTHS_WINDOW).map { offset ->
                val month = startMonth.plusMonths(offset.toLong())
                val key = month.toString()
                MonthlyPlaytime(
                    yearMonth = key,
                    totalMinutes = byMonth[key]?.totalMinutes ?: 0
                )
            }
        }
    }
}

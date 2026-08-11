package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.DayActivity
import com.rogue.scorequest.utils.toEpochMillis
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DAYS_WINDOW = 90L

class GetActivityHeatmapUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(): Flow<List<DayActivity>> {
        val today = LocalDate.now()
        val startDay = today.minusDays(DAYS_WINDOW - 1)
        val sinceEpoch = startDay.atStartOfDay().toEpochMillis()

        return repository.getActivityByDaySince(sinceEpoch).map { rows ->
            val byDay = rows.associateBy { it.day }
            (0 until DAYS_WINDOW).map { offset ->
                val day = startDay.plusDays(offset)
                val key = day.toString()
                DayActivity(day = key, sessionCount = byDay[key]?.sessionCount ?: 0)
            }
        }
    }
}

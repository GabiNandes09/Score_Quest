package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.MonthSessionCount
import java.time.Year
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSessionsByMonthUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(): Flow<List<MonthSessionCount>> {
        val year = Year.now().value.toString()

        return repository.getSessionCountByYear(year).map { rows ->
            val byMonth = rows.associateBy { it.month }
            (1..12).map { month ->
                val key = month.toString().padStart(2, '0')
                MonthSessionCount(month = key, sessionCount = byMonth[key]?.sessionCount ?: 0)
            }
        }
    }
}

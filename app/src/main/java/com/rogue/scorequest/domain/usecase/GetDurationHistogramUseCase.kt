package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.DurationBucket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val BUCKET_LABELS = listOf("0-30min", "30-60min", "1-2h", "2h+")

class GetDurationHistogramUseCase(
    private val repository: GameSessionRepository
) {
    operator fun invoke(): Flow<List<DurationBucket>> {
        return repository.getAllSessionDurations().map { durations ->
            val counts = durations.groupingBy { bucketFor(it) }.eachCount()
            BUCKET_LABELS.map { label -> DurationBucket(label = label, sessionCount = counts[label] ?: 0) }
        }
    }

    private fun bucketFor(minutes: Int): String = when {
        minutes <= 30 -> BUCKET_LABELS[0]
        minutes <= 60 -> BUCKET_LABELS[1]
        minutes <= 120 -> BUCKET_LABELS[2]
        else -> BUCKET_LABELS[3]
    }
}

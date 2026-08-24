package com.rogue.scorequest.domain.model

data class ActiveTimer(
    val gameId: String,
    val gameName: String,
    val startedAtMillis: Long,
    val status: TimerStatus,
    val pausedAtMillis: Long? = null,
    val accumulatedPausedMillis: Long = 0L
) {
    fun elapsedMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val referenceEnd = if (status == TimerStatus.PAUSED) pausedAtMillis ?: nowMillis else nowMillis
        return (referenceEnd - startedAtMillis - accumulatedPausedMillis).coerceAtLeast(0L)
    }
}

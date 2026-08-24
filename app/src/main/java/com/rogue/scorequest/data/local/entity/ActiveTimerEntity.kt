package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rogue.scorequest.domain.model.ActiveTimer
import com.rogue.scorequest.domain.model.TimerStatus

@Entity(tableName = "active_timer")
data class ActiveTimerEntity(
    @PrimaryKey val id: String = SINGLETON_ID,
    @ColumnInfo(name = "game_id") val gameId: String,
    @ColumnInfo(name = "game_name") val gameName: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    val status: TimerStatus,
    @ColumnInfo(name = "paused_at") val pausedAt: Long?,
    @ColumnInfo(name = "accumulated_paused_millis") val accumulatedPausedMillis: Long
) {
    companion object {
        const val SINGLETON_ID = "active"
    }
}

fun ActiveTimerEntity.toDomain() = ActiveTimer(
    gameId = gameId,
    gameName = gameName,
    startedAtMillis = startedAt,
    status = status,
    pausedAtMillis = pausedAt,
    accumulatedPausedMillis = accumulatedPausedMillis
)

fun ActiveTimer.toEntity() = ActiveTimerEntity(
    gameId = gameId,
    gameName = gameName,
    startedAt = startedAtMillis,
    status = status,
    pausedAt = pausedAtMillis,
    accumulatedPausedMillis = accumulatedPausedMillis
)

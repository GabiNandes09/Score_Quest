package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.domain.model.GameSession
import com.rogue.scorequest.domain.model.ScoreEntry
import com.rogue.scorequest.domain.model.ScoreInput
import java.time.LocalDateTime
import java.util.UUID

class SaveGameSessionUseCase(
    private val repository: GameSessionRepository
) {
    suspend operator fun invoke(
        gameId: String,
        date: LocalDateTime,
        durationMinutes: Int,
        variantOrExpansion: String?,
        photoUri: String?,
        groupId: String?,
        scores: List<ScoreInput>
    ): String {
        val sessionId = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        val session = GameSession(
            id = sessionId,
            gameId = gameId,
            date = date,
            durationMinutes = durationMinutes,
            variantOrExpansion = variantOrExpansion,
            photoUri = photoUri,
            groupId = groupId,
            participantIds = scores.map { it.playerId },
            createdAt = now,
            updatedAt = now
        )
        val scoreEntries = scores.map { input ->
            ScoreEntry(
                sessionId = sessionId,
                playerId = input.playerId,
                totalScore = input.totalScore,
                isWinner = input.isWinner,
                fieldValues = input.fieldValues,
                createdAt = now,
                updatedAt = now
            )
        }
        repository.saveSession(session, scoreEntries)
        return sessionId
    }
}

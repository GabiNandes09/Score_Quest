package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.GameScoreSchemaRepository
import com.rogue.scorequest.domain.model.GameScoreSchema
import com.rogue.scorequest.domain.model.ScoreFieldType
import com.rogue.scorequest.domain.model.ScoreFormula
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.UserProfile
import com.rogue.scorequest.domain.model.WinnerMode
import java.time.LocalDateTime
import java.util.UUID

class SaveGameScoreSchemaUseCase(
    private val repository: GameScoreSchemaRepository
) {
    suspend operator fun invoke(
        gameId: String,
        type: ScoreSchemaType,
        fields: List<ScoreFieldType>,
        winnerMode: WinnerMode,
        formula: ScoreFormula?
    ) {
        val existing = repository.findByGameIdOnce(gameId)
        val now = LocalDateTime.now()
        val schema = GameScoreSchema(
            id = existing?.id ?: UUID.randomUUID().toString(),
            gameId = gameId,
            type = type,
            fields = fields,
            winnerMode = winnerMode,
            formula = formula,
            createdByUserId = existing?.createdByUserId ?: UserProfile.LOCAL_PROFILE_ID,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        repository.save(schema)
    }
}

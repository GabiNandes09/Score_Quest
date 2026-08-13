package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.data.repository.GameScoreSchemaRepository
import com.rogue.scorequest.data.seed.SeedGamesFile
import com.rogue.scorequest.data.seed.toSeedGame
import com.rogue.scorequest.data.seed.toSeedScoreSchema
import com.rogue.scorequest.domain.model.ExportResult
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Exporta o acervo de jogos + schemas de pontuação personalizados no mesmo
 * formato JSON consumido por ImportSeedGamesUseCase (ver Regras/Jogos
 * iniciais.json) — permite backup/compartilhamento e reimportação posterior.
 */
class ExportGamesUseCase(
    private val boardGameRepository: BoardGameRepository,
    private val gameScoreSchemaRepository: GameScoreSchemaRepository
) {
    private val json = Json { prettyPrint = true }

    suspend operator fun invoke(): ExportResult {
        val games = boardGameRepository.getGames().first().map { it.game }
        val schemas = gameScoreSchemaRepository.getAllOnce()

        val file = SeedGamesFile(
            games = games.map { it.toSeedGame() },
            scoreSchemas = schemas.map { it.toSeedScoreSchema() }
        )

        return ExportResult(
            json = json.encodeToString(file),
            gamesCount = games.size,
            schemasCount = schemas.size
        )
    }
}

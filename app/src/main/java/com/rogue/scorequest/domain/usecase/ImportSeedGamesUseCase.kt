package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.data.repository.GameScoreSchemaRepository
import com.rogue.scorequest.data.seed.SeedGame
import com.rogue.scorequest.data.seed.SeedGamesFile
import com.rogue.scorequest.data.seed.SeedScoreSchema
import com.rogue.scorequest.data.seed.toDomain
import com.rogue.scorequest.domain.model.BoardGame
import com.rogue.scorequest.domain.model.GameScoreSchema
import com.rogue.scorequest.domain.model.GameSource
import com.rogue.scorequest.domain.model.ImportResult
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.UserLibraryEntry
import com.rogue.scorequest.domain.model.UserProfile
import com.rogue.scorequest.domain.model.WinnerMode
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.serialization.json.Json

/**
 * Importa jogos + schemas de pontuação de um arquivo JSON (formato descrito
 * em Regras/Jogos iniciais.json) pras tabelas locais do app. Adição vs.
 * atualização é decidida pelo `id` do jogo e pelo `gameId` do schema —
 * reimportar o mesmo arquivo atualiza os registros existentes em vez de
 * duplicá-los.
 */
class ImportSeedGamesUseCase(
    private val boardGameRepository: BoardGameRepository,
    private val gameScoreSchemaRepository: GameScoreSchemaRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend operator fun invoke(jsonContent: String): ImportResult {
        val file = try {
            json.decodeFromString<SeedGamesFile>(jsonContent)
        } catch (e: Exception) {
            return ImportResult(errors = listOf("Não foi possível ler o JSON: ${e.message}"))
        }

        var gamesAdded = 0
        var gamesUpdated = 0
        var schemasImported = 0
        val errors = mutableListOf<String>()

        file.games.forEach { seedGame ->
            try {
                if (importGame(seedGame)) gamesUpdated++ else gamesAdded++
            } catch (e: Exception) {
                errors += "Jogo \"${seedGame.name}\": ${e.message}"
            }
        }

        file.scoreSchemas.forEach { seedSchema ->
            try {
                importSchema(seedSchema)
                schemasImported++
            } catch (e: Exception) {
                errors += "Pontuação de \"${seedSchema.gameId}\": ${e.message}"
            }
        }

        return ImportResult(gamesAdded, gamesUpdated, schemasImported, errors)
    }

    /** @return true se atualizou um jogo existente, false se inseriu um novo. */
    private suspend fun importGame(seedGame: SeedGame): Boolean {
        val now = LocalDateTime.now()
        val existing = boardGameRepository.findGameOnce(seedGame.id)

        if (existing != null) {
            boardGameRepository.updateGame(
                existing.copy(
                    name = seedGame.name,
                    minPlayers = seedGame.minPlayers,
                    maxPlayers = seedGame.maxPlayers,
                    avgDurationMinutes = seedGame.avgDurationMinutes,
                    coverImageUrl = seedGame.coverImageUrl,
                    category = seedGame.category,
                    weight = seedGame.weight,
                    updatedAt = now
                )
            )
            return true
        }

        boardGameRepository.insertGame(
            BoardGame(
                id = seedGame.id,
                name = seedGame.name,
                minPlayers = seedGame.minPlayers,
                maxPlayers = seedGame.maxPlayers,
                avgDurationMinutes = seedGame.avgDurationMinutes,
                coverImageUrl = seedGame.coverImageUrl,
                category = seedGame.category,
                weight = seedGame.weight,
                source = runCatching { GameSource.valueOf(seedGame.source.uppercase()) }.getOrDefault(GameSource.CURATED),
                createdAt = now,
                updatedAt = now
            )
        )
        boardGameRepository.upsertLibraryEntry(
            UserLibraryEntry(
                gameId = seedGame.id,
                status = LibraryStatus.WANT,
                played = false,
                createdAt = now,
                updatedAt = now
            )
        )
        return false
    }

    private suspend fun importSchema(seedSchema: SeedScoreSchema) {
        val now = LocalDateTime.now()
        val existing = gameScoreSchemaRepository.findByGameIdOnce(seedSchema.gameId)
        val schema = GameScoreSchema(
            id = existing?.id ?: UUID.randomUUID().toString(),
            gameId = seedSchema.gameId,
            type = ScoreSchemaType.valueOf(seedSchema.type.uppercase()),
            fields = seedSchema.fields.map { it.toDomain() },
            winnerMode = WinnerMode.valueOf(seedSchema.winnerMode.uppercase()),
            formula = seedSchema.formula?.toDomain(),
            createdByUserId = existing?.createdByUserId ?: UserProfile.LOCAL_PROFILE_ID,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        gameScoreSchemaRepository.save(schema)
    }
}

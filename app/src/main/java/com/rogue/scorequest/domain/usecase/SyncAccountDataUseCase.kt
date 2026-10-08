package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudGame
import com.rogue.scorequest.data.cloud.toCloud
import com.rogue.scorequest.data.cloud.toDomain
import com.rogue.scorequest.data.cloud.toDomainSessionAndScores
import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.data.repository.CloudBackupRepository
import com.rogue.scorequest.data.repository.GameScoreSchemaRepository
import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.data.repository.PlayerGroupRepository
import com.rogue.scorequest.data.repository.PlayerRepository
import com.rogue.scorequest.data.repository.ProfileRepository
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.domain.model.Player
import kotlinx.coroutines.flow.first

/**
 * Roda logo após o login. Dois escopos tratados de formas diferentes (ver
 * CLAUDE.md "Conta e sincronização com Firebase"):
 * - **Catálogo compartilhado** (jogos + pontuação personalizada): sempre
 *   sincronizado nos dois sentidos — sobe o que existe localmente (upsert por
 *   id, idempotente) e baixa o catálogo inteiro, já que é global entre todos
 *   os usuários, não pertence a esta conta.
 * - **Dado pessoal** (jogadores, grupos, partidas, perfil, estante): só um
 *   sentido por vez, sem merge bidirecional — se o Room local já tem
 *   jogadores cadastrados, ele é a fonte da verdade e sobe pra nuvem
 *   (sobrescreve o backup anterior desta conta); se está vazio (ex.: reabriu
 *   a conta depois de um logout, que limpa tudo), baixa o backup existente da
 *   nuvem pro Room.
 */
class SyncAccountDataUseCase(
    private val boardGameRepository: BoardGameRepository,
    private val playerRepository: PlayerRepository,
    private val playerGroupRepository: PlayerGroupRepository,
    private val gameSessionRepository: GameSessionRepository,
    private val gameScoreSchemaRepository: GameScoreSchemaRepository,
    private val profileRepository: ProfileRepository,
    private val cloudBackupRepository: CloudBackupRepository
) {

    suspend operator fun invoke(uid: String) {
        val gamesWithLibrary = boardGameRepository.getGames().first()
        syncSharedCatalog(gamesWithLibrary)

        val players = playerRepository.getPlayers().first()
        if (players.isNotEmpty()) {
            pushPersonalData(uid, gamesWithLibrary, players)
        } else {
            pullPersonalData(uid)
        }
    }

    private suspend fun syncSharedCatalog(gamesWithLibrary: List<GameWithLibraryInfo>) {
        cloudBackupRepository.uploadGames(gamesWithLibrary.map { it.game.toCloud() })
        cloudBackupRepository.uploadScoreSchemas(gameScoreSchemaRepository.getAllOnce().map { it.toCloud() })

        cloudBackupRepository.downloadGames().forEach { upsertCatalogGame(it) }
        cloudBackupRepository.downloadScoreSchemas().forEach { gameScoreSchemaRepository.save(it.toDomain()) }
    }

    /** Upsert por id (mesmo idioma do ImportSeedGamesUseCase) — ao contrário do dado pessoal, o catálogo pode já ter o jogo localmente mesmo fora do caso "local vazio". */
    private suspend fun upsertCatalogGame(cloudGame: CloudGame) {
        val incoming = cloudGame.toDomain()
        val existing = boardGameRepository.findGameOnce(incoming.id)
        if (existing != null) {
            boardGameRepository.updateGame(incoming.copy(createdAt = existing.createdAt))
        } else {
            boardGameRepository.insertGame(incoming)
        }
    }

    private suspend fun pushPersonalData(uid: String, gamesWithLibrary: List<GameWithLibraryInfo>, players: List<Player>) {
        profileRepository.getProfile().first()?.let { cloudBackupRepository.uploadProfile(uid, it.toCloud()) }
        cloudBackupRepository.uploadLibraryEntries(uid, gamesWithLibrary.mapNotNull { it.libraryEntry?.toCloud() })
        cloudBackupRepository.uploadPlayers(uid, players.map { it.toCloud() })
        cloudBackupRepository.uploadGroups(uid, playerGroupRepository.getGroupsOnce().map { it.toCloud() })
        cloudBackupRepository.uploadSessions(uid, gameSessionRepository.getAllSessionsOnce().map { it.toCloud() })
        cloudBackupRepository.uploadFavoriteGameIds(uid, profileRepository.getFavoriteGames().first().map { it.id })
    }

    private suspend fun pullPersonalData(uid: String) {
        cloudBackupRepository.downloadProfile(uid)?.let { profileRepository.saveProfile(it.toDomain()) }
        cloudBackupRepository.downloadLibraryEntries(uid).forEach { boardGameRepository.upsertLibraryEntry(it.toDomain()) }
        cloudBackupRepository.downloadPlayers(uid).forEach { playerRepository.insertPlayer(it.toDomain()) }
        cloudBackupRepository.downloadGroups(uid).forEach { playerGroupRepository.createGroup(it.toDomain()) }
        cloudBackupRepository.downloadSessions(uid).forEach { cloudSession ->
            val (session, scores) = cloudSession.toDomainSessionAndScores()
            gameSessionRepository.saveSession(session, scores)
        }
        cloudBackupRepository.downloadFavoriteGameIds(uid).forEach { profileRepository.addFavorite(it) }
    }
}

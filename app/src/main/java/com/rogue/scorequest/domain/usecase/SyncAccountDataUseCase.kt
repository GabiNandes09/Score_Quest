package com.rogue.scorequest.domain.usecase

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
 * Roda logo após o login: se o aparelho já tem dado local (acumulado antes de
 * logar), ele é a fonte da verdade e sobe pra nuvem daquele usuário (sobrescreve
 * o backup anterior, se houver). Se o local está vazio (ex.: reabriu a conta
 * depois de um logout, que limpa tudo via ClearLocalDataUseCase), baixa o
 * backup existente da nuvem pro Room. Sem merge bidirecional — ver CLAUDE.md.
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
        val games = boardGameRepository.getGames().first()
        val players = playerRepository.getPlayers().first()
        if (games.isNotEmpty() || players.isNotEmpty()) {
            pushToCloud(uid, games, players)
        } else {
            pullFromCloud(uid)
        }
    }

    private suspend fun pushToCloud(uid: String, gamesWithLibrary: List<GameWithLibraryInfo>, players: List<Player>) {
        profileRepository.getProfile().first()?.let { cloudBackupRepository.uploadProfile(uid, it.toCloud()) }
        cloudBackupRepository.uploadGames(uid, gamesWithLibrary.map { it.game.toCloud() })
        cloudBackupRepository.uploadLibraryEntries(uid, gamesWithLibrary.mapNotNull { it.libraryEntry?.toCloud() })
        cloudBackupRepository.uploadPlayers(uid, players.map { it.toCloud() })
        cloudBackupRepository.uploadGroups(uid, playerGroupRepository.getGroupsOnce().map { it.toCloud() })
        cloudBackupRepository.uploadSessions(uid, gameSessionRepository.getAllSessionsOnce().map { it.toCloud() })
        cloudBackupRepository.uploadScoreSchemas(uid, gameScoreSchemaRepository.getAllOnce().map { it.toCloud() })
        cloudBackupRepository.uploadFavoriteGameIds(uid, profileRepository.getFavoriteGames().first().map { it.id })
    }

    private suspend fun pullFromCloud(uid: String) {
        cloudBackupRepository.downloadProfile(uid)?.let { profileRepository.saveProfile(it.toDomain()) }
        cloudBackupRepository.downloadGames(uid).forEach { boardGameRepository.insertGame(it.toDomain()) }
        cloudBackupRepository.downloadLibraryEntries(uid).forEach { boardGameRepository.upsertLibraryEntry(it.toDomain()) }
        cloudBackupRepository.downloadPlayers(uid).forEach { playerRepository.insertPlayer(it.toDomain()) }
        cloudBackupRepository.downloadGroups(uid).forEach { playerGroupRepository.createGroup(it.toDomain()) }
        cloudBackupRepository.downloadScoreSchemas(uid).forEach { gameScoreSchemaRepository.save(it.toDomain()) }
        cloudBackupRepository.downloadSessions(uid).forEach { cloudSession ->
            val (session, scores) = cloudSession.toDomainSessionAndScores()
            gameSessionRepository.saveSession(session, scores)
        }
        cloudBackupRepository.downloadFavoriteGameIds(uid).forEach { profileRepository.addFavorite(it) }
    }
}

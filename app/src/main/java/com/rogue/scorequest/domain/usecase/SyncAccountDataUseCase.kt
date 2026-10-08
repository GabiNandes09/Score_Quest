package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudBackup
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    private val json = Json { ignoreUnknownKeys = true }

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
        val backup = CloudBackup(
            profile = profileRepository.getProfile().first()?.toCloud(),
            games = gamesWithLibrary.map { it.game.toCloud() },
            libraryEntries = gamesWithLibrary.mapNotNull { it.libraryEntry?.toCloud() },
            players = players.map { it.toCloud() },
            groups = playerGroupRepository.getGroupsOnce().map { it.toCloud() },
            sessions = gameSessionRepository.getAllSessionsOnce().map { it.toCloud() },
            scoreSchemas = gameScoreSchemaRepository.getAllOnce().map { it.toCloud() },
            favoriteGameIds = profileRepository.getFavoriteGames().first().map { it.id }
        )
        cloudBackupRepository.upload(uid, json.encodeToString(backup))
    }

    private suspend fun pullFromCloud(uid: String) {
        val payload = cloudBackupRepository.download(uid) ?: return
        val backup = runCatching { json.decodeFromString<CloudBackup>(payload) }.getOrNull() ?: return

        backup.profile?.let { profileRepository.saveProfile(it.toDomain()) }
        backup.games.forEach { boardGameRepository.insertGame(it.toDomain()) }
        backup.libraryEntries.forEach { boardGameRepository.upsertLibraryEntry(it.toDomain()) }
        backup.players.forEach { playerRepository.insertPlayer(it.toDomain()) }
        backup.groups.forEach { playerGroupRepository.createGroup(it.toDomain()) }
        backup.scoreSchemas.forEach { gameScoreSchemaRepository.save(it.toDomain()) }
        backup.sessions.forEach { cloudSession ->
            val (session, scores) = cloudSession.toDomainSessionAndScores()
            gameSessionRepository.saveSession(session, scores)
        }
        backup.favoriteGameIds.forEach { profileRepository.addFavorite(it) }
    }
}

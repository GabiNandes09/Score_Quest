package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudFriend
import com.rogue.scorequest.data.repository.PlayerRepository
import com.rogue.scorequest.domain.model.Player
import java.time.LocalDateTime
import java.util.UUID

/**
 * Reaproveita o Player local já vinculado a esse amigo (`linkedUserId`), se
 * existir; só cria um novo se nenhum vínculo prévio for encontrado — evita
 * duplicar jogador toda vez que o amigo é selecionado de novo numa partida.
 */
class GetOrCreatePlayerForFriendUseCase(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(friend: CloudFriend): Player {
        playerRepository.findByLinkedUserId(friend.uid)?.let { return it }

        val now = LocalDateTime.now()
        val player = Player(
            id = UUID.randomUUID().toString(),
            nickname = friend.displayName?.takeIf { it.isNotBlank() } ?: friend.username,
            linkedUserId = friend.uid,
            avatarPath = friend.photoUrl,
            createdAt = now,
            updatedAt = now
        )
        playerRepository.insertPlayer(player)
        return player
    }
}

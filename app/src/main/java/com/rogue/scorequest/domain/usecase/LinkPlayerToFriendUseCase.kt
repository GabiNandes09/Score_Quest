package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudFriend
import com.rogue.scorequest.data.repository.PlayerRepository
import com.rogue.scorequest.domain.model.Player
import java.time.LocalDateTime

/**
 * "Merge" pedido pelo usuário: um Player local que já existia (com histórico
 * de partidas) passa a representar um amigo de verdade — só seta
 * `linkedUserId`, o `Player.id` continua o mesmo, então nenhuma GameSession/
 * ScoreEntry antiga precisa mudar.
 */
class LinkPlayerToFriendUseCase(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(player: Player, friend: CloudFriend) {
        playerRepository.updatePlayer(player.copy(linkedUserId = friend.uid, updatedAt = LocalDateTime.now()))
    }
}

package com.rogue.scorequest.data.cloud

import com.rogue.scorequest.domain.model.FriendRequestStatus
import kotlinx.serialization.Serializable

/**
 * Solicitação de amizade (`friendRequests/{id}`, raiz do Firestore — coleção
 * neutra que os dois lados podem ler, já que a regra de segurança não deixa
 * um usuário escrever direto no documento de outro). Campos do remetente/
 * destinatário são **espelhados na hora de criar** (não um read adicional no
 * momento de aceitar) — evita precisar reler `publicProfiles` de ninguém só
 * pra montar o registro de amizade.
 */
@Serializable
data class CloudFriendRequest(
    val id: String = "",
    val fromUid: String,
    val fromUsername: String,
    val fromDisplayName: String? = null,
    val fromPhotoUrl: String? = null,
    val toUid: String,
    val toUsername: String,
    val toDisplayName: String? = null,
    val toPhotoUrl: String? = null,
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    val createdAt: Long,
    val respondedAt: Long? = null
)

/**
 * Registro de amizade (`users/{uid}/friends/{friendUid}`) — espelhado nos dois
 * lados ao aceitar uma solicitação (ver FriendRepository.addFriendPair), cada
 * lado guarda uma cópia denormalizada dos dados públicos do outro pra listar
 * sem precisar de leitura extra em `publicProfiles`.
 */
@Serializable
data class CloudFriend(
    val uid: String,
    val displayName: String? = null,
    val username: String,
    val photoUrl: String? = null,
    val since: Long
)

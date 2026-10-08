package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.data.cloud.CloudFriend
import com.rogue.scorequest.data.cloud.CloudFriendRequest
import com.rogue.scorequest.data.cloud.CloudPublicProfile

data class FriendsState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResult: CloudPublicProfile? = null,
    val searchError: String? = null,
    val sendRequestMessage: String? = null,
    val incomingRequests: List<CloudFriendRequest> = emptyList(),
    val friends: List<CloudFriend> = emptyList(),
    // uids de amigos que já têm um Player local vinculado (ver GetOrCreatePlayerForFriendUseCase) —
    // controla se a linha mostra "Adicionar como jogador" ou já aparece marcado como tal.
    val linkedFriendUids: Set<String> = emptySet()
)

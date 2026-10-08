package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.data.cloud.CloudFriend

data class AddEditPlayerState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val nickname: String = "",
    val avatarPath: String? = null,
    val deleteError: String? = null,
    // "Vincular a um amigo" (merge) — ver CLAUDE.md. linkedUserId espelha Player.linkedUserId;
    // linkedUsername é só pra exibição (resolvido contra a lista de amigos carregada).
    val linkedUserId: String? = null,
    val linkedUsername: String? = null,
    val isLoggedIn: Boolean = false,
    val showLinkFriendDialog: Boolean = false,
    val availableFriendsToLink: List<CloudFriend> = emptyList()
) {
    val isValid: Boolean get() = nickname.isNotBlank()
}

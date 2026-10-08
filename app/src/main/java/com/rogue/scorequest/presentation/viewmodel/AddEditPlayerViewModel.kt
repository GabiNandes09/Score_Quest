package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.data.cloud.CloudFriend
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.usecase.CreatePlayerUseCase
import com.rogue.scorequest.domain.usecase.DeletePlayerUseCase
import com.rogue.scorequest.domain.usecase.GetAuthStateUseCase
import com.rogue.scorequest.domain.usecase.GetFriendsUseCase
import com.rogue.scorequest.domain.usecase.GetPlayerUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.LinkPlayerToFriendUseCase
import com.rogue.scorequest.domain.usecase.UpdatePlayerUseCase
import com.rogue.scorequest.presentation.navigation.Routes
import com.rogue.scorequest.presentation.viewmodel.states.AddEditPlayerState
import com.rogue.scorequest.utils.ImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditPlayerViewModel(
    private val playerId: String,
    getPlayer: GetPlayerUseCase,
    private val createPlayer: CreatePlayerUseCase,
    private val updatePlayer: UpdatePlayerUseCase,
    private val deletePlayer: DeletePlayerUseCase,
    getAuthState: GetAuthStateUseCase,
    private val getPlayersUseCase: GetPlayersUseCase,
    private val getFriends: GetFriendsUseCase,
    private val linkPlayerToFriend: LinkPlayerToFriendUseCase
) : ViewModel() {

    val isEditMode: Boolean = playerId != Routes.AddEditPlayer.NEW_PLAYER

    private val _state = MutableStateFlow(AddEditPlayerState(isEditMode = isEditMode, isLoading = isEditMode))
    val state = _state.asStateFlow()

    private var originalPlayer: Player? = null
    private var originalAvatarPath: String? = null
    private var myFriends: List<CloudFriend> = emptyList()

    init {
        if (isEditMode) {
            viewModelScope.launch {
                getPlayer(playerId).collect { player ->
                    if (player != null) {
                        originalPlayer = player
                        originalAvatarPath = player.avatarPath
                        _state.update {
                            it.copy(
                                isLoading = false,
                                nickname = player.nickname,
                                avatarPath = player.avatarPath,
                                linkedUserId = player.linkedUserId
                            )
                        }
                        refreshLinkedUsername()
                    }
                }
            }
        }

        viewModelScope.launch {
            getAuthState().collect { user ->
                _state.update { it.copy(isLoggedIn = user != null) }
                myFriends = if (user != null) getFriends(user.uid) else emptyList()
                refreshLinkedUsername()
            }
        }
    }

    private fun refreshLinkedUsername() {
        val linkedUid = _state.value.linkedUserId
        _state.update { it.copy(linkedUsername = myFriends.find { friend -> friend.uid == linkedUid }?.username) }
    }

    fun onNicknameChange(value: String) = _state.update { it.copy(nickname = value) }

    fun onAvatarCaptured(path: String) {
        val pendingPath = _state.value.avatarPath
        if (pendingPath != null && pendingPath != originalAvatarPath) {
            ImageStorage.deleteImage(pendingPath)
        }
        _state.update { it.copy(avatarPath = path) }
    }

    fun onOpenLinkFriendDialog() {
        viewModelScope.launch {
            // Amigo que já tem outro Player vinculado não entra na lista — evita duplicar vínculo.
            val linkedUids = getPlayersUseCase().first().mapNotNull { it.linkedUserId }.toSet()
            val available = myFriends.filter { it.uid !in linkedUids }
            _state.update { it.copy(availableFriendsToLink = available, showLinkFriendDialog = true) }
        }
    }

    fun onDismissLinkFriendDialog() = _state.update { it.copy(showLinkFriendDialog = false) }

    fun onLinkToFriend(friend: CloudFriend) {
        val player = originalPlayer ?: return
        viewModelScope.launch {
            linkPlayerToFriend(player, friend)
            originalPlayer = player.copy(linkedUserId = friend.uid)
            _state.update {
                it.copy(linkedUserId = friend.uid, linkedUsername = friend.username, showLinkFriendDialog = false)
            }
        }
    }

    fun save() {
        val current = _state.value
        if (!current.isValid || current.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            if (isEditMode) {
                val player = originalPlayer ?: return@launch
                updatePlayer(player, current.nickname.trim(), current.avatarPath)
                if (originalAvatarPath != null && originalAvatarPath != current.avatarPath) {
                    ImageStorage.deleteImage(originalAvatarPath)
                }
            } else {
                createPlayer(current.nickname.trim(), current.avatarPath)
            }
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }

    fun delete() {
        if (!isEditMode) return
        viewModelScope.launch {
            val success = deletePlayer(playerId)
            _state.update {
                if (success) {
                    it.copy(deleted = true)
                } else {
                    it.copy(deleteError = "Não é possível excluir um jogador com histórico de partidas")
                }
            }
        }
    }

    fun dismissDeleteError() = _state.update { it.copy(deleteError = null) }
}

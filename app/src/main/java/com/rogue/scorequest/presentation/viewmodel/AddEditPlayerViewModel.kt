package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.usecase.CreatePlayerUseCase
import com.rogue.scorequest.domain.usecase.DeletePlayerUseCase
import com.rogue.scorequest.domain.usecase.GetPlayerUseCase
import com.rogue.scorequest.domain.usecase.UpdatePlayerUseCase
import com.rogue.scorequest.presentation.navigation.Routes
import com.rogue.scorequest.presentation.viewmodel.states.AddEditPlayerState
import com.rogue.scorequest.utils.ImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditPlayerViewModel(
    private val playerId: String,
    getPlayer: GetPlayerUseCase,
    private val createPlayer: CreatePlayerUseCase,
    private val updatePlayer: UpdatePlayerUseCase,
    private val deletePlayer: DeletePlayerUseCase
) : ViewModel() {

    val isEditMode: Boolean = playerId != Routes.AddEditPlayer.NEW_PLAYER

    private val _state = MutableStateFlow(AddEditPlayerState(isEditMode = isEditMode, isLoading = isEditMode))
    val state = _state.asStateFlow()

    private var originalPlayer: Player? = null
    private var originalAvatarPath: String? = null

    init {
        if (isEditMode) {
            viewModelScope.launch {
                getPlayer(playerId).collect { player ->
                    if (player != null) {
                        originalPlayer = player
                        originalAvatarPath = player.avatarPath
                        _state.update {
                            it.copy(isLoading = false, nickname = player.nickname, avatarPath = player.avatarPath)
                        }
                    }
                }
            }
        }
    }

    fun onNicknameChange(value: String) = _state.update { it.copy(nickname = value) }

    fun onAvatarCaptured(path: String) {
        val pendingPath = _state.value.avatarPath
        if (pendingPath != null && pendingPath != originalAvatarPath) {
            ImageStorage.deleteImage(pendingPath)
        }
        _state.update { it.copy(avatarPath = path) }
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

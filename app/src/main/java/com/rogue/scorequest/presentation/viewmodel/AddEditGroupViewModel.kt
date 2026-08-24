package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.PlayerGroup
import com.rogue.scorequest.domain.usecase.CreatePlayerGroupUseCase
import com.rogue.scorequest.domain.usecase.DeletePlayerGroupUseCase
import com.rogue.scorequest.domain.usecase.GetPlayerGroupUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.UpdatePlayerGroupUseCase
import com.rogue.scorequest.presentation.navigation.Routes
import com.rogue.scorequest.presentation.viewmodel.states.AddEditGroupState
import com.rogue.scorequest.utils.ImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditGroupViewModel(
    private val groupId: String,
    getGroup: GetPlayerGroupUseCase,
    getPlayers: GetPlayersUseCase,
    private val createGroup: CreatePlayerGroupUseCase,
    private val updateGroup: UpdatePlayerGroupUseCase,
    private val deleteGroup: DeletePlayerGroupUseCase
) : ViewModel() {

    val isEditMode: Boolean = groupId != Routes.AddEditGroup.NEW_GROUP

    private val _state = MutableStateFlow(AddEditGroupState(isEditMode = isEditMode, isLoading = isEditMode))
    val state = _state.asStateFlow()

    val players: StateFlow<List<Player>> = getPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var originalGroup: PlayerGroup? = null
    private var originalPhotoPath: String? = null

    init {
        if (isEditMode) {
            viewModelScope.launch {
                getGroup(groupId).collect { group ->
                    if (group != null) {
                        originalGroup = group
                        originalPhotoPath = group.photoPath
                        _state.update {
                            it.copy(
                                isLoading = false,
                                name = group.name,
                                photoPath = group.photoPath,
                                selectedMemberIds = group.memberIds.toSet()
                            )
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }

    fun onPhotoCaptured(path: String) {
        val pendingPath = _state.value.photoPath
        if (pendingPath != null && pendingPath != originalPhotoPath) {
            ImageStorage.deleteImage(pendingPath)
        }
        _state.update { it.copy(photoPath = path) }
    }

    fun onMemberToggled(playerId: String) {
        val current = _state.value.selectedMemberIds
        val updated = if (playerId in current) current - playerId else current + playerId
        _state.update { it.copy(selectedMemberIds = updated) }
    }

    fun save() {
        val current = _state.value
        if (!current.isValid || current.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            if (isEditMode) {
                val group = originalGroup ?: return@launch
                updateGroup(group, current.name.trim(), current.photoPath, current.selectedMemberIds.toList())
                if (originalPhotoPath != null && originalPhotoPath != current.photoPath) {
                    ImageStorage.deleteImage(originalPhotoPath)
                }
            } else {
                createGroup(current.name.trim(), current.photoPath, current.selectedMemberIds.toList())
            }
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }

    fun delete() {
        if (!isEditMode) return
        viewModelScope.launch {
            deleteGroup(groupId)
            _state.update { it.copy(deleted = true) }
        }
    }
}

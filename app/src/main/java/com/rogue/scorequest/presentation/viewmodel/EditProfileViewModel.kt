package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.usecase.GetProfileUseCase
import com.rogue.scorequest.domain.usecase.UpdateProfileUseCase
import com.rogue.scorequest.presentation.viewmodel.states.EditProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getProfile().collect { profile ->
                if (profile != null) {
                    _state.update {
                        it.copy(displayName = profile.displayName, bio = profile.bio.orEmpty(), avatarUri = profile.avatarUri)
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(displayName = value) }
    fun onBioChange(value: String) = _state.update { it.copy(bio = value) }
    fun onAvatarCaptured(path: String) = _state.update { it.copy(avatarUri = path) }

    fun save() {
        viewModelScope.launch {
            val current = _state.value
            updateProfile(current.displayName.trim(), current.bio.trim().ifBlank { null }, current.avatarUri)
            _state.update { it.copy(saved = true) }
        }
    }
}

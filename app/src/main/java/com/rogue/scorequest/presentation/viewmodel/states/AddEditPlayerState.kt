package com.rogue.scorequest.presentation.viewmodel.states

data class AddEditPlayerState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val nickname: String = "",
    val avatarPath: String? = null,
    val deleteError: String? = null
) {
    val isValid: Boolean get() = nickname.isNotBlank()
}

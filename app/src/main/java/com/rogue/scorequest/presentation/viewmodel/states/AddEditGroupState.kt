package com.rogue.scorequest.presentation.viewmodel.states

data class AddEditGroupState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val name: String = "",
    val photoPath: String? = null,
    val selectedMemberIds: Set<String> = emptySet()
) {
    val isValid: Boolean get() = name.isNotBlank() && selectedMemberIds.size >= 2
}

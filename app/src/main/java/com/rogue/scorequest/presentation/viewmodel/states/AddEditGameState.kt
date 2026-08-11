package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.LibraryStatus

data class AddEditGameState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val name: String = "",
    val minPlayers: String = "",
    val maxPlayers: String = "",
    val avgDurationMinutes: String = "",
    val category: String = "",
    val weight: String = "",
    val coverImagePath: String? = null,
    val status: LibraryStatus? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() && (isEditMode || status != null) &&
            minPlayers.toIntOrNull() != null && maxPlayers.toIntOrNull() != null &&
            avgDurationMinutes.toIntOrNull() != null
}

package com.rogue.scorequest.presentation.viewmodel.states

import java.time.LocalDate

data class ScoreEntryInput(
    val totalScore: String = "",
    val isWinner: Boolean = false
)

data class AddSessionState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,

    val selectedGameId: String? = null,
    val selectedGameName: String = "",

    val date: LocalDate = LocalDate.now(),
    val durationMinutes: String = "",
    val variantOrExpansion: String = "",
    val photoPath: String? = null,

    val selectedPlayerIds: List<String> = emptyList(),
    val scores: Map<String, ScoreEntryInput> = emptyMap()
) {
    val canProceedFromGameStep: Boolean get() = selectedGameId != null
    val canProceedFromSessionDataStep: Boolean get() = durationMinutes.toIntOrNull() != null
    val canProceedFromPlayersStep: Boolean get() = selectedPlayerIds.isNotEmpty()
}

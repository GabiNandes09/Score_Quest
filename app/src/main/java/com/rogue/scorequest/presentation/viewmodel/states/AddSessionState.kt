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
    val scores: Map<String, ScoreEntryInput> = emptyMap(),

    // Pontuação composta (schema COMPOSITE, ver seção 8 do doc de produto)
    val compositeFieldValues: Map<String, Map<String, String>> = emptyMap(), // playerId -> fieldKey -> valor
    val manualWinnerId: String? = null, // winnerMode = MANUAL
    val automaticWinnerIds: Set<String> = emptySet(), // winnerMode = AUTOMATIC, já resolvido (inclui empate considerado)
    val pendingTieCandidateIds: List<String> = emptyList() // winnerMode = AUTOMATIC, aguardando decisão do usuário
) {
    val canProceedFromGameStep: Boolean get() = selectedGameId != null
    val canProceedFromSessionDataStep: Boolean get() = durationMinutes.toIntOrNull() != null
    val canProceedFromPlayersStep: Boolean get() = selectedPlayerIds.isNotEmpty()
}

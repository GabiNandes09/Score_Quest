package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.ActiveTimer

data class LiveMatchState(
    val isLoading: Boolean = true,
    val timer: ActiveTimer? = null,
    val conflictingTimer: ActiveTimer? = null, // outra partida já em andamento (jogo diferente)
    val showCancelConfirmation: Boolean = false,
    val cancelled: Boolean = false, // true assim que "Cancelar" for confirmado, dispara a navegação de volta
    val finishedDurationMinutes: Int? = null // não-nulo assim que "Finalizar" concluir, dispara a navegação
)

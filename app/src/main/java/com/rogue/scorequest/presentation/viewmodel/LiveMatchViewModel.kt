package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.usecase.CancelTimerUseCase
import com.rogue.scorequest.domain.usecase.FinishTimerUseCase
import com.rogue.scorequest.domain.usecase.GetActiveTimerUseCase
import com.rogue.scorequest.domain.usecase.GetGamesUseCase
import com.rogue.scorequest.domain.usecase.PauseTimerUseCase
import com.rogue.scorequest.domain.usecase.ResumeTimerUseCase
import com.rogue.scorequest.domain.usecase.StartTimerUseCase
import com.rogue.scorequest.presentation.viewmodel.states.LiveMatchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LiveMatchViewModel(
    private val gameId: String,
    private val getGamesUseCase: GetGamesUseCase,
    private val getActiveTimerUseCase: GetActiveTimerUseCase,
    private val startTimerUseCase: StartTimerUseCase,
    private val pauseTimerUseCase: PauseTimerUseCase,
    private val resumeTimerUseCase: ResumeTimerUseCase,
    private val cancelTimerUseCase: CancelTimerUseCase,
    private val finishTimerUseCase: FinishTimerUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LiveMatchState())
    val state = _state.asStateFlow()

    // Distingue "ainda não existe linha, precisa criar" de "existia e sumiu porque foi
    // cancelada/finalizada enquanto essa tela estava aberta" — as duas batem com
    // `timer == null`, mas só a primeira deve chamar StartTimerUseCase. Fica true assim
    // que QUALQUER timer real é observado, seja porque essa tela criou um novo (fluxo
    // "iniciar") ou porque já existia um ao entrar (fluxo "retomar" via banner da Home) —
    // sem isso, retomar e depois cancelar recriava a partida na hora (a 1ª emissão já vinha
    // não-nula nesse fluxo, então a flag antiga nunca era marcada).
    private var hasSeenActiveTimer = false

    init {
        viewModelScope.launch {
            getActiveTimerUseCase().collect { timer ->
                when {
                    timer == null && !hasSeenActiveTimer -> {
                        hasSeenActiveTimer = true
                        val gameName = getGamesUseCase().first().find { it.game.id == gameId }?.game?.name ?: gameId
                        startTimerUseCase(gameId, gameName)
                    }
                    timer != null && timer.gameId == gameId -> {
                        hasSeenActiveTimer = true
                        _state.value = _state.value.copy(isLoading = false, timer = timer, conflictingTimer = null)
                    }
                    timer != null && timer.gameId != gameId -> {
                        // Partida ativa é de outro jogo (ex.: chegou aqui por um back-stack
                        // antigo) — não sobrescreve, deixa a tela mostrar o conflito.
                        hasSeenActiveTimer = true
                        _state.value = _state.value.copy(isLoading = false, timer = null, conflictingTimer = timer)
                    }
                    else -> {
                        // timer == null && hasSeenActiveTimer: foi cancelada ou finalizada
                        // (e a partida salva) enquanto essa tela existia — não reinicia.
                        _state.value = _state.value.copy(isLoading = false, timer = null)
                    }
                }
            }
        }
    }

    fun onPause() {
        viewModelScope.launch { pauseTimerUseCase() }
    }

    fun onResume() {
        viewModelScope.launch { resumeTimerUseCase() }
    }

    fun onRequestCancel() {
        _state.value = _state.value.copy(showCancelConfirmation = true)
    }

    fun onDismissCancelConfirmation() {
        _state.value = _state.value.copy(showCancelConfirmation = false)
    }

    fun onConfirmCancel() {
        viewModelScope.launch {
            cancelTimerUseCase()
            _state.value = _state.value.copy(showCancelConfirmation = false, cancelled = true)
        }
    }

    fun onFinish() {
        viewModelScope.launch {
            val minutes = finishTimerUseCase() ?: 0
            _state.value = _state.value.copy(finishedDurationMinutes = minutes)
        }
    }
}

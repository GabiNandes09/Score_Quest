package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.SessionWithDetails
import com.rogue.scorequest.domain.usecase.DeleteGameSessionUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.GetSessionDetailUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    private val sessionId: String,
    getSessionDetail: GetSessionDetailUseCase,
    getPlayers: GetPlayersUseCase,
    private val deleteGameSession: DeleteGameSessionUseCase
) : ViewModel() {

    val sessionDetail: StateFlow<SessionWithDetails?> = getSessionDetail(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val players: StateFlow<List<Player>> = getPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteGameSession(sessionId)
            onDeleted()
        }
    }
}

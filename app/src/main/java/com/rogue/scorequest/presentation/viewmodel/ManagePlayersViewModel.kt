package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.usecase.DeletePlayerUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.UpdatePlayerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManagePlayersViewModel(
    getPlayers: GetPlayersUseCase,
    private val updatePlayer: UpdatePlayerUseCase,
    private val deletePlayer: DeletePlayerUseCase
) : ViewModel() {

    val players: StateFlow<List<Player>> = getPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun rename(player: Player, newNickname: String) {
        if (newNickname.isBlank()) return
        viewModelScope.launch { updatePlayer(player, newNickname.trim()) }
    }

    fun delete(playerId: String) {
        viewModelScope.launch {
            val success = deletePlayer(playerId)
            if (!success) {
                _errorMessage.value = "Não é possível excluir um jogador com histórico de partidas"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.usecase.GetFavoriteGamesUseCase
import com.rogue.scorequest.domain.usecase.GetGamesUseCase
import com.rogue.scorequest.domain.usecase.SetFavoriteGameUseCase
import com.rogue.scorequest.presentation.viewmodel.states.EditFavoritesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditFavoritesViewModel(
    getGames: GetGamesUseCase,
    getFavoriteGames: GetFavoriteGamesUseCase,
    private val setFavoriteGame: SetFavoriteGameUseCase
) : ViewModel() {

    val state = combine(getGames(), getFavoriteGames()) { games, favorites ->
        EditFavoritesState(
            allGames = games.map { it.game },
            favoriteIds = favorites.map { it.id }.toSet()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EditFavoritesState())

    private val _pendingReplacementGameId = MutableStateFlow<String?>(null)
    val pendingReplacementGameId = _pendingReplacementGameId.asStateFlow()

    fun toggleFavorite(gameId: String) {
        viewModelScope.launch {
            if (gameId in state.value.favoriteIds) {
                setFavoriteGame.remove(gameId)
            } else {
                val added = setFavoriteGame.add(gameId)
                if (!added) {
                    _pendingReplacementGameId.value = gameId
                }
            }
        }
    }

    fun confirmReplace() {
        val gameId = _pendingReplacementGameId.value ?: return
        viewModelScope.launch {
            setFavoriteGame.replaceOldestWith(gameId)
            _pendingReplacementGameId.value = null
        }
    }

    fun cancelReplace() {
        _pendingReplacementGameId.value = null
    }
}

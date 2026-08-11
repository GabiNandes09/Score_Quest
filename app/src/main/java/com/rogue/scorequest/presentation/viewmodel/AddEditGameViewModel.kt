package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.usecase.AddUserGameUseCase
import com.rogue.scorequest.domain.usecase.GetGameDetailUseCase
import com.rogue.scorequest.domain.usecase.UpdateUserGameUseCase
import com.rogue.scorequest.presentation.navigation.Routes
import com.rogue.scorequest.presentation.viewmodel.states.AddEditGameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditGameViewModel(
    private val gameId: String,
    getGameDetail: GetGameDetailUseCase,
    private val addUserGame: AddUserGameUseCase,
    private val updateUserGame: UpdateUserGameUseCase
) : ViewModel() {

    val isEditMode: Boolean = gameId != Routes.AddEditGame.NEW_GAME

    private val _state = MutableStateFlow(AddEditGameState(isEditMode = isEditMode, isLoading = isEditMode))
    val state = _state.asStateFlow()

    init {
        if (isEditMode) {
            viewModelScope.launch {
                getGameDetail(gameId).collect { detail ->
                    if (detail != null) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                name = detail.game.name,
                                minPlayers = detail.game.minPlayers.toString(),
                                maxPlayers = detail.game.maxPlayers.toString(),
                                avgDurationMinutes = detail.game.avgDurationMinutes.toString(),
                                category = detail.game.category.orEmpty(),
                                weight = detail.game.weight?.toString().orEmpty(),
                                coverImagePath = detail.game.coverImageUrl
                            )
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onMinPlayersChange(value: String) = _state.update { it.copy(minPlayers = value) }
    fun onMaxPlayersChange(value: String) = _state.update { it.copy(maxPlayers = value) }
    fun onDurationChange(value: String) = _state.update { it.copy(avgDurationMinutes = value) }
    fun onCategoryChange(value: String) = _state.update { it.copy(category = value) }
    fun onWeightChange(value: String) = _state.update { it.copy(weight = value) }
    fun onCoverCaptured(path: String) = _state.update { it.copy(coverImagePath = path) }
    fun onStatusSelected(status: LibraryStatus) = _state.update { it.copy(status = status) }

    fun save() {
        val current = _state.value
        if (!current.isValid || current.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            if (isEditMode) {
                updateUserGame(
                    gameId = gameId,
                    name = current.name.trim(),
                    minPlayers = current.minPlayers.toInt(),
                    maxPlayers = current.maxPlayers.toInt(),
                    avgDurationMinutes = current.avgDurationMinutes.toInt(),
                    category = current.category.trim().ifBlank { null },
                    weight = current.weight.toDoubleOrNull(),
                    coverImageUrl = current.coverImagePath
                )
            } else {
                addUserGame(
                    name = current.name.trim(),
                    minPlayers = current.minPlayers.toInt(),
                    maxPlayers = current.maxPlayers.toInt(),
                    avgDurationMinutes = current.avgDurationMinutes.toInt(),
                    category = current.category.trim().ifBlank { null },
                    weight = current.weight.toDoubleOrNull(),
                    coverImageUrl = current.coverImagePath,
                    initialStatus = requireNotNull(current.status)
                )
            }
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }
}

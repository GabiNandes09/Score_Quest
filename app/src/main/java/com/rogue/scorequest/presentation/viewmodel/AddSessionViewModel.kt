package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.ScoreInput
import com.rogue.scorequest.domain.usecase.CreatePlayerUseCase
import com.rogue.scorequest.domain.usecase.GetGamesUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.GetSessionDetailUseCase
import com.rogue.scorequest.domain.usecase.SaveGameSessionUseCase
import com.rogue.scorequest.domain.usecase.UpdateGameSessionUseCase
import com.rogue.scorequest.presentation.navigation.Routes
import com.rogue.scorequest.presentation.viewmodel.states.AddSessionState
import com.rogue.scorequest.presentation.viewmodel.states.ScoreEntryInput
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddSessionViewModel(
    val sessionId: String,
    val initialGameId: String,
    getGamesUseCase: GetGamesUseCase,
    getPlayersUseCase: GetPlayersUseCase,
    private val createPlayerUseCase: CreatePlayerUseCase,
    private val saveGameSessionUseCase: SaveGameSessionUseCase,
    private val updateGameSessionUseCase: UpdateGameSessionUseCase,
    private val getSessionDetailUseCase: GetSessionDetailUseCase
) : ViewModel() {

    val isEditMode: Boolean = sessionId != Routes.AddSessionWizardGraph.NEW_SESSION
    private val hasPreselectedGame: Boolean =
        !isEditMode && initialGameId != Routes.AddSessionWizardGraph.NO_GAME

    private var autoAdvancePending = hasPreselectedGame

    private val _state = MutableStateFlow(
        AddSessionState(
            isEditMode = isEditMode,
            isLoading = isEditMode,
            selectedGameId = initialGameId.takeIf { hasPreselectedGame }
        )
    )
    val state = _state.asStateFlow()

    val games: StateFlow<List<GameWithLibraryInfo>> = getGamesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val players: StateFlow<List<Player>> = getPlayersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var originalCreatedAt: LocalDateTime = LocalDateTime.now()

    init {
        if (isEditMode) {
            viewModelScope.launch {
                getSessionDetailUseCase(sessionId).collect { detail ->
                    if (detail != null) {
                        originalCreatedAt = detail.session.createdAt
                        _state.value = _state.value.copy(
                            isLoading = false,
                            selectedGameId = detail.session.gameId,
                            selectedGameName = detail.gameName,
                            date = detail.session.date.toLocalDate(),
                            durationMinutes = detail.session.durationMinutes.toString(),
                            variantOrExpansion = detail.session.variantOrExpansion.orEmpty(),
                            photoPath = detail.session.photoUri,
                            selectedPlayerIds = detail.session.participantIds,
                            scores = detail.scores.associate { score ->
                                score.playerId to ScoreEntryInput(
                                    totalScore = score.totalScore?.toString().orEmpty(),
                                    isWinner = score.isWinner ?: false
                                )
                            }
                        )
                    }
                }
            }
        }

        if (hasPreselectedGame) {
            viewModelScope.launch {
                games.collect { list ->
                    val current = _state.value
                    if (current.selectedGameId == initialGameId && current.selectedGameName.isBlank()) {
                        list.find { it.game.id == initialGameId }?.let { found ->
                            _state.value = current.copy(selectedGameName = found.game.name)
                        }
                    }
                }
            }
        }
    }

    fun consumeAutoAdvance(): Boolean {
        if (autoAdvancePending) {
            autoAdvancePending = false
            return true
        }
        return false
    }

    fun onGameSelected(gameId: String, gameName: String) {
        _state.value = _state.value.copy(selectedGameId = gameId, selectedGameName = gameName)
    }

    fun onDateChange(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
    }

    fun onDurationChange(value: String) {
        _state.value = _state.value.copy(durationMinutes = value)
    }

    fun onVariantChange(value: String) {
        _state.value = _state.value.copy(variantOrExpansion = value)
    }

    fun onPhotoCaptured(path: String) {
        _state.value = _state.value.copy(photoPath = path)
    }

    fun onPlayerToggled(playerId: String) {
        val current = _state.value.selectedPlayerIds
        val updatedIds = if (playerId in current) current - playerId else current + playerId
        val updatedScores = _state.value.scores.filterKeys { it in updatedIds }.toMutableMap()
        updatedIds.filter { it !in updatedScores }.forEach { updatedScores[it] = ScoreEntryInput() }
        _state.value = _state.value.copy(selectedPlayerIds = updatedIds, scores = updatedScores)
    }

    fun createAndAddPlayer(nickname: String) {
        if (nickname.isBlank()) return
        viewModelScope.launch {
            val player = createPlayerUseCase(nickname.trim())
            onPlayerToggled(player.id)
        }
    }

    fun onScoreChange(playerId: String, score: String) {
        val updated = _state.value.scores.toMutableMap()
        updated[playerId] = (updated[playerId] ?: ScoreEntryInput()).copy(totalScore = score)
        _state.value = _state.value.copy(scores = updated)
    }

    fun onWinnerToggled(playerId: String) {
        val updated = _state.value.scores.toMutableMap()
        val entry = updated[playerId] ?: ScoreEntryInput()
        updated[playerId] = entry.copy(isWinner = !entry.isWinner)
        _state.value = _state.value.copy(scores = updated)
    }

    fun save() {
        val current = _state.value
        val gameId = current.selectedGameId ?: return
        if (current.isSaving) return

        val duration = current.durationMinutes.toIntOrNull() ?: 0
        val scoreInputs = current.selectedPlayerIds.map { playerId ->
            val entry = current.scores[playerId] ?: ScoreEntryInput()
            ScoreInput(playerId = playerId, totalScore = entry.totalScore.toIntOrNull(), isWinner = entry.isWinner)
        }

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true)
            if (isEditMode) {
                updateGameSessionUseCase(
                    sessionId = sessionId,
                    gameId = gameId,
                    date = current.date.atStartOfDay(),
                    durationMinutes = duration,
                    variantOrExpansion = current.variantOrExpansion.trim().ifBlank { null },
                    photoUri = current.photoPath,
                    createdAt = originalCreatedAt,
                    scores = scoreInputs
                )
            } else {
                saveGameSessionUseCase(
                    gameId = gameId,
                    date = current.date.atStartOfDay(),
                    durationMinutes = duration,
                    variantOrExpansion = current.variantOrExpansion.trim().ifBlank { null },
                    photoUri = current.photoPath,
                    scores = scoreInputs
                )
            }
            _state.value = _state.value.copy(isSaving = false, saved = true)
        }
    }
}

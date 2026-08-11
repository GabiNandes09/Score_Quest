package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.ComparisonRule
import com.rogue.scorequest.domain.model.GameScoreSchema
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.ScoreInput
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.WinnerMode
import com.rogue.scorequest.domain.usecase.CalculateScoreFormulaUseCase
import com.rogue.scorequest.domain.usecase.CreatePlayerUseCase
import com.rogue.scorequest.domain.usecase.GetGameScoreSchemaUseCase
import com.rogue.scorequest.domain.usecase.GetGamesUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.GetSessionDetailUseCase
import com.rogue.scorequest.domain.usecase.SaveGameSessionUseCase
import com.rogue.scorequest.domain.usecase.UpdateGameSessionUseCase
import com.rogue.scorequest.presentation.navigation.Routes
import com.rogue.scorequest.presentation.viewmodel.states.AddSessionState
import com.rogue.scorequest.presentation.viewmodel.states.ScoreEntryInput
import com.rogue.scorequest.utils.ImageStorage
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AddSessionViewModel(
    val sessionId: String,
    val initialGameId: String,
    getGamesUseCase: GetGamesUseCase,
    getPlayersUseCase: GetPlayersUseCase,
    private val createPlayerUseCase: CreatePlayerUseCase,
    private val saveGameSessionUseCase: SaveGameSessionUseCase,
    private val updateGameSessionUseCase: UpdateGameSessionUseCase,
    private val getSessionDetailUseCase: GetSessionDetailUseCase,
    getGameScoreSchemaUseCase: GetGameScoreSchemaUseCase,
    private val calculateScoreFormulaUseCase: CalculateScoreFormulaUseCase
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

    val schema: StateFlow<GameScoreSchema?> = _state
        .map { it.selectedGameId }
        .distinctUntilChanged()
        .flatMapLatest { gameId -> if (gameId != null) getGameScoreSchemaUseCase(gameId) else flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var originalCreatedAt: LocalDateTime = LocalDateTime.now()
    private var originalPhotoPath: String? = null

    init {
        if (isEditMode) {
            viewModelScope.launch {
                getSessionDetailUseCase(sessionId).collect { detail ->
                    if (detail != null) {
                        originalCreatedAt = detail.session.createdAt
                        originalPhotoPath = detail.session.photoUri
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
                            },
                            compositeFieldValues = detail.scores.associate { it.playerId to it.fieldValues.orEmpty() },
                            manualWinnerId = detail.scores.find { it.isWinner == true }?.playerId,
                            automaticWinnerIds = detail.scores.filter { it.isWinner == true }.map { it.playerId }.toSet()
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
        val pendingPath = _state.value.photoPath
        if (pendingPath != null && pendingPath != originalPhotoPath) {
            ImageStorage.deleteImage(pendingPath)
        }
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

    fun onCompositeFieldChange(playerId: String, fieldKey: String, value: String) {
        val current = _state.value
        val playerValues = (current.compositeFieldValues[playerId] ?: emptyMap()) + (fieldKey to value)
        _state.value = current.copy(
            compositeFieldValues = current.compositeFieldValues + (playerId to playerValues),
            // valores mudaram: descarta resolução automática/empate anterior, será recalculada
            automaticWinnerIds = emptySet(),
            pendingTieCandidateIds = emptyList()
        )
    }

    fun onManualWinnerSelected(playerId: String) {
        _state.value = _state.value.copy(manualWinnerId = playerId)
    }

    fun resolveAutomaticWinnerIfNeeded() {
        val current = _state.value
        val currentSchema = schema.value ?: return
        if (currentSchema.type != ScoreSchemaType.COMPOSITE || currentSchema.winnerMode != WinnerMode.AUTOMATIC) return
        if (current.automaticWinnerIds.isNotEmpty() || current.pendingTieCandidateIds.isNotEmpty()) return

        val totals = current.selectedPlayerIds.associateWith { playerId ->
            calculateScoreFormulaUseCase(currentSchema, current.compositeFieldValues[playerId] ?: emptyMap()) ?: 0
        }
        if (totals.isEmpty()) return
        val best = if (currentSchema.formula?.comparisonRule == ComparisonRule.LOWEST_WINS) {
            totals.values.min()
        } else {
            totals.values.max()
        }
        val candidates = totals.filterValues { it == best }.keys.toList()
        _state.value = if (candidates.size > 1) {
            current.copy(pendingTieCandidateIds = candidates)
        } else {
            current.copy(automaticWinnerIds = candidates.toSet())
        }
    }

    fun calculateTotalFor(playerId: String): Int? {
        val currentSchema = schema.value ?: return null
        val fieldValues = _state.value.compositeFieldValues[playerId] ?: emptyMap()
        return calculateScoreFormulaUseCase(currentSchema, fieldValues)
    }

    fun resolveTieAsDoubleWinner() {
        _state.value = _state.value.copy(
            automaticWinnerIds = _state.value.pendingTieCandidateIds.toSet(),
            pendingTieCandidateIds = emptyList()
        )
    }

    fun resolveTieManually(playerId: String) {
        _state.value = _state.value.copy(
            automaticWinnerIds = setOf(playerId),
            pendingTieCandidateIds = emptyList()
        )
    }

    fun save() {
        val current = _state.value
        val gameId = current.selectedGameId ?: return
        if (current.isSaving) return

        val duration = current.durationMinutes.toIntOrNull() ?: 0
        val currentSchema = schema.value

        val scoreInputs = if (currentSchema != null && currentSchema.type == ScoreSchemaType.COMPOSITE) {
            current.selectedPlayerIds.map { playerId ->
                val fieldValues = current.compositeFieldValues[playerId] ?: emptyMap()
                val totalScore = if (currentSchema.winnerMode == WinnerMode.AUTOMATIC) {
                    calculateScoreFormulaUseCase(currentSchema, fieldValues)
                } else {
                    null
                }
                val isWinner = when (currentSchema.winnerMode) {
                    WinnerMode.MANUAL -> playerId == current.manualWinnerId
                    WinnerMode.AUTOMATIC -> playerId in current.automaticWinnerIds
                    WinnerMode.NONE -> null
                }
                ScoreInput(playerId = playerId, totalScore = totalScore, isWinner = isWinner, fieldValues = fieldValues)
            }
        } else {
            current.selectedPlayerIds.map { playerId ->
                val entry = current.scores[playerId] ?: ScoreEntryInput()
                ScoreInput(playerId = playerId, totalScore = entry.totalScore.toIntOrNull(), isWinner = entry.isWinner)
            }
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
                if (originalPhotoPath != null && originalPhotoPath != current.photoPath) {
                    ImageStorage.deleteImage(originalPhotoPath)
                }
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

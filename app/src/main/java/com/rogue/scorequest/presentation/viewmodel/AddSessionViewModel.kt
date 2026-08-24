package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.ComparisonRule
import com.rogue.scorequest.domain.model.GameScoreSchema
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.RANKING_POINTS_FIELD_KEY
import com.rogue.scorequest.domain.model.RANKING_POSITION_FIELD_KEY
import com.rogue.scorequest.domain.model.ScoreInput
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.WinnerMode
import com.rogue.scorequest.domain.usecase.CalculateScoreFormulaUseCase
import com.rogue.scorequest.domain.usecase.ClearActiveTimerForGameUseCase
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
    val initialDurationMinutes: String,
    getGamesUseCase: GetGamesUseCase,
    getPlayersUseCase: GetPlayersUseCase,
    private val createPlayerUseCase: CreatePlayerUseCase,
    private val saveGameSessionUseCase: SaveGameSessionUseCase,
    private val updateGameSessionUseCase: UpdateGameSessionUseCase,
    private val getSessionDetailUseCase: GetSessionDetailUseCase,
    getGameScoreSchemaUseCase: GetGameScoreSchemaUseCase,
    private val calculateScoreFormulaUseCase: CalculateScoreFormulaUseCase,
    private val clearActiveTimerForGameUseCase: ClearActiveTimerForGameUseCase
) : ViewModel() {

    val isEditMode: Boolean = sessionId != Routes.AddSessionWizardGraph.NEW_SESSION
    private val hasPreselectedGame: Boolean =
        !isEditMode && initialGameId != Routes.AddSessionWizardGraph.NO_GAME

    private var autoAdvancePending = hasPreselectedGame

    private val hasPrefillDuration: Boolean =
        hasPreselectedGame && initialDurationMinutes != Routes.AddSessionWizardGraph.NO_DURATION

    private val _state = MutableStateFlow(
        AddSessionState(
            isEditMode = isEditMode,
            isLoading = isEditMode,
            selectedGameId = initialGameId.takeIf { hasPreselectedGame },
            durationMinutes = initialDurationMinutes.takeIf { hasPrefillDuration }.orEmpty()
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
                            automaticWinnerIds = detail.scores.filter { it.isWinner == true }.map { it.playerId }.toSet(),
                            // Heurística: schema pode ser COMPOSITE mas essa partida não tem
                            // nenhum fieldValues salvo -> foi registrada em modo Simples.
                            useSimpleEntry = detail.scores.isNotEmpty() && detail.scores.all { it.fieldValues.isNullOrEmpty() },
                            // Heurística equivalente pro Ranking: só sessões Ranking gravam a
                            // chave "position" em fieldValues, então a presença dela já basta
                            // pra reconstruir a ordem sem precisar esperar o schema carregar.
                            rankingOrder = detail.scores
                                .filter { it.fieldValues?.containsKey(RANKING_POSITION_FIELD_KEY) == true }
                                .sortedBy { it.fieldValues?.get(RANKING_POSITION_FIELD_KEY)?.toIntOrNull() ?: Int.MAX_VALUE }
                                .map { it.playerId },
                            rankingPoints = detail.scores.associate {
                                it.playerId to (it.fieldValues?.get(RANKING_POINTS_FIELD_KEY) ?: it.totalScore?.toString().orEmpty())
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
        val pendingPath = _state.value.photoPath
        if (pendingPath != null && pendingPath != originalPhotoPath) {
            ImageStorage.deleteImage(pendingPath)
        }
        _state.value = _state.value.copy(photoPath = path)
    }

    fun onPlayerToggled(playerId: String) {
        val state = _state.value
        val current = state.selectedPlayerIds
        val updatedIds = if (playerId in current) current - playerId else current + playerId
        val updatedScores = state.scores.filterKeys { it in updatedIds }.toMutableMap()
        updatedIds.filter { it !in updatedScores }.forEach { updatedScores[it] = ScoreEntryInput() }
        // Mantém a ordem de ranking em sincronia: remove quem foi desmarcado, acrescenta
        // quem foi marcado agora no fim da lista (sem mexer na ordem de quem já estava).
        val updatedRankingOrder = state.rankingOrder.filter { it in updatedIds } +
            updatedIds.filter { it !in state.rankingOrder }
        val updatedRankingPoints = state.rankingPoints.filterKeys { it in updatedIds }
        _state.value = state.copy(
            selectedPlayerIds = updatedIds,
            scores = updatedScores,
            rankingOrder = updatedRankingOrder,
            rankingPoints = updatedRankingPoints
        )
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

    fun onToggleSimpleEntry() {
        _state.value = _state.value.copy(useSimpleEntry = !_state.value.useSimpleEntry)
    }

    fun ensureRankingOrderInitialized() {
        val current = _state.value
        if (current.rankingOrder.isEmpty() && current.selectedPlayerIds.isNotEmpty()) {
            _state.value = current.copy(rankingOrder = current.selectedPlayerIds)
        }
    }

    fun onRankingReordered(newOrder: List<String>) {
        _state.value = _state.value.copy(rankingOrder = newOrder)
    }

    fun onMoveRankingUp(index: Int) {
        if (index <= 0) return
        val updated = _state.value.rankingOrder.toMutableList()
        val moved = updated.removeAt(index)
        updated.add(index - 1, moved)
        _state.value = _state.value.copy(rankingOrder = updated)
    }

    fun onMoveRankingDown(index: Int) {
        val order = _state.value.rankingOrder
        if (index >= order.lastIndex) return
        val updated = order.toMutableList()
        val moved = updated.removeAt(index)
        updated.add(index + 1, moved)
        _state.value = _state.value.copy(rankingOrder = updated)
    }

    fun onRankingPointsChanged(playerId: String, value: String) {
        _state.value = _state.value.copy(rankingPoints = _state.value.rankingPoints + (playerId to value))
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

        val scoreInputs = when {
            currentSchema != null && currentSchema.type == ScoreSchemaType.COMPOSITE && !current.useSimpleEntry -> {
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
            }
            currentSchema != null && currentSchema.type == ScoreSchemaType.RANKING -> {
                val hasPointsField = currentSchema.fields.isNotEmpty()
                val order = current.rankingOrder.ifEmpty { current.selectedPlayerIds }
                order.mapIndexed { index, playerId ->
                    val position = index + 1
                    val pointsText = current.rankingPoints[playerId]
                    val points = if (hasPointsField) pointsText?.toIntOrNull() else null
                    val fieldValues = buildMap {
                        put(RANKING_POSITION_FIELD_KEY, position.toString())
                        if (hasPointsField) put(RANKING_POINTS_FIELD_KEY, pointsText.orEmpty())
                    }
                    ScoreInput(playerId = playerId, totalScore = points, isWinner = position == 1, fieldValues = fieldValues)
                }
            }
            else -> {
                current.selectedPlayerIds.map { playerId ->
                    val entry = current.scores[playerId] ?: ScoreEntryInput()
                    ScoreInput(playerId = playerId, totalScore = entry.totalScore.toIntOrNull(), isWinner = entry.isWinner)
                }
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
                // Partida veio de um cronômetro finalizado (ver Routes.NO_DURATION):
                // só agora, com os dados de fato salvos, o cronômetro é encerrado de
                // verdade. Até aqui ele ficou só pausado (ver FinishTimerUseCase), pra
                // não reiniciar do zero se o usuário saísse do wizard sem salvar.
                if (hasPrefillDuration) {
                    clearActiveTimerForGameUseCase(gameId)
                }
            }
            _state.value = _state.value.copy(isSaving = false, saved = true)
        }
    }
}

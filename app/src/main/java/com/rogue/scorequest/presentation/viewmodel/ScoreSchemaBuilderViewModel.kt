package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.ComparisonRule
import com.rogue.scorequest.domain.model.DuplicableSchema
import com.rogue.scorequest.domain.model.GameScoreSchema
import com.rogue.scorequest.domain.model.RANKING_POINTS_FIELD_KEY
import com.rogue.scorequest.domain.model.ScoreFieldType
import com.rogue.scorequest.domain.model.ScoreFormula
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.ScoreTerm
import com.rogue.scorequest.domain.model.WinnerMode
import com.rogue.scorequest.domain.usecase.CalculateScoreFormulaUseCase
import com.rogue.scorequest.domain.usecase.GetDuplicableSchemasUseCase
import com.rogue.scorequest.domain.usecase.GetGameDetailUseCase
import com.rogue.scorequest.domain.usecase.GetGameScoreSchemaUseCase
import com.rogue.scorequest.domain.usecase.SaveGameScoreSchemaUseCase
import com.rogue.scorequest.presentation.viewmodel.states.BuilderStep
import com.rogue.scorequest.presentation.viewmodel.states.ScoreSchemaBuilderState
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScoreSchemaBuilderViewModel(
    private val gameId: String,
    getGameDetail: GetGameDetailUseCase,
    getGameScoreSchema: GetGameScoreSchemaUseCase,
    getDuplicableSchemas: GetDuplicableSchemasUseCase,
    private val saveGameScoreSchema: SaveGameScoreSchemaUseCase,
    private val calculateScoreFormula: CalculateScoreFormulaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScoreSchemaBuilderState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getGameDetail(gameId).collect { detail ->
                if (detail != null) {
                    _state.update { it.copy(gameName = detail.game.name) }
                }
            }
        }
        viewModelScope.launch {
            getGameScoreSchema(gameId).collect { schema ->
                if (schema != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            type = schema.type,
                            fields = schema.fields,
                            winnerMode = schema.winnerMode,
                            formulaTerms = schema.formula?.terms.orEmpty(),
                            comparisonRule = schema.formula?.comparisonRule ?: ComparisonRule.HIGHEST_WINS
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
        viewModelScope.launch {
            getDuplicableSchemas().collect { list ->
                _state.update { it.copy(duplicableSchemas = list.filter { d -> d.schema.gameId != gameId }) }
            }
        }
    }

    fun onSimpleChosen() {
        _state.update { it.copy(type = ScoreSchemaType.SIMPLE, fields = emptyList(), formulaTerms = emptyList()) }
    }

    fun onCompositeChosen() {
        _state.update { it.copy(type = ScoreSchemaType.COMPOSITE, step = BuilderStep.FIELD_LIST) }
    }

    fun onRankingChosen() {
        _state.update { it.copy(type = ScoreSchemaType.RANKING, fields = emptyList(), formulaTerms = emptyList()) }
    }

    fun onToggleRankingPoints() {
        _state.update { current ->
            val fields = if (current.fields.isEmpty()) {
                listOf(ScoreFieldType.NumberField(key = RANKING_POINTS_FIELD_KEY, label = "Pontos"))
            } else {
                emptyList()
            }
            current.copy(fields = fields)
        }
    }

    fun onDuplicateSelected(duplicable: DuplicableSchema) {
        val schema = duplicable.schema
        _state.update {
            it.copy(
                type = ScoreSchemaType.COMPOSITE,
                fields = schema.fields,
                winnerMode = schema.winnerMode,
                formulaTerms = schema.formula?.terms.orEmpty(),
                comparisonRule = schema.formula?.comparisonRule ?: ComparisonRule.HIGHEST_WINS,
                step = BuilderStep.FIELD_LIST
            )
        }
    }

    fun addField(field: ScoreFieldType) {
        _state.update { it.copy(fields = it.fields + field) }
    }

    fun updateField(index: Int, field: ScoreFieldType) {
        _state.update { current ->
            val updated = current.fields.toMutableList().also { it[index] = field }
            current.copy(fields = updated)
        }
    }

    fun removeField(index: Int) {
        _state.update { current ->
            val removedKey = current.fields.getOrNull(index)?.key
            val updated = current.fields.toMutableList().also { it.removeAt(index) }
            current.copy(
                fields = updated,
                formulaTerms = current.formulaTerms.filterNot { it.fieldKey == removedKey }
            )
        }
    }

    fun moveFieldUp(index: Int) {
        if (index <= 0) return
        _state.update { current ->
            val updated = current.fields.toMutableList()
            val moved = updated.removeAt(index)
            updated.add(index - 1, moved)
            current.copy(fields = updated)
        }
    }

    fun moveFieldDown(index: Int) {
        _state.update { current ->
            if (index >= current.fields.lastIndex) return@update current
            val updated = current.fields.toMutableList()
            val moved = updated.removeAt(index)
            updated.add(index + 1, moved)
            current.copy(fields = updated)
        }
    }

    fun goToStep(step: BuilderStep) {
        _state.update { it.copy(step = step) }
    }

    fun onWinnerModeChosen(mode: WinnerMode) {
        _state.update { it.copy(winnerMode = mode) }
    }

    fun onComparisonRuleChosen(rule: ComparisonRule) {
        _state.update { it.copy(comparisonRule = rule) }
    }

    fun addFormulaTerm(fieldKey: String) {
        _state.update { current ->
            if (current.formulaTerms.any { it.fieldKey == fieldKey }) return@update current
            current.copy(formulaTerms = current.formulaTerms + ScoreTerm(fieldKey = fieldKey))
        }
    }

    fun removeFormulaTerm(fieldKey: String) {
        _state.update { it.copy(formulaTerms = it.formulaTerms.filterNot { term -> term.fieldKey == fieldKey }) }
    }

    fun updateFormulaTermWeight(fieldKey: String, weight: Double) {
        _state.update { current ->
            current.copy(
                formulaTerms = current.formulaTerms.map { term ->
                    if (term.fieldKey == fieldKey) term.copy(weight = weight) else term
                }
            )
        }
    }

    fun calculateTestTotal(fieldValues: Map<String, String>): Int? {
        val current = _state.value
        if (current.winnerMode != WinnerMode.AUTOMATIC) return null
        val now = LocalDateTime.now()
        val schema = GameScoreSchema(
            id = "",
            gameId = gameId,
            type = ScoreSchemaType.COMPOSITE,
            fields = current.fields,
            winnerMode = WinnerMode.AUTOMATIC,
            formula = ScoreFormula(terms = current.formulaTerms, comparisonRule = current.comparisonRule),
            createdByUserId = "",
            createdAt = now,
            updatedAt = now
        )
        return calculateScoreFormula(schema, fieldValues)
    }

    fun save() {
        val current = _state.value
        val type = current.type ?: return
        if (current.isSaving) return

        val formula = if (type == ScoreSchemaType.COMPOSITE &&
            current.winnerMode == WinnerMode.AUTOMATIC &&
            current.formulaTerms.isNotEmpty()
        ) {
            ScoreFormula(terms = current.formulaTerms, comparisonRule = current.comparisonRule)
        } else {
            null
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            saveGameScoreSchema(
                gameId = gameId,
                type = type,
                fields = if (type == ScoreSchemaType.SIMPLE) emptyList() else current.fields,
                winnerMode = if (type == ScoreSchemaType.SIMPLE || type == ScoreSchemaType.RANKING) {
                    WinnerMode.AUTOMATIC
                } else {
                    current.winnerMode
                },
                formula = formula
            )
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }
}

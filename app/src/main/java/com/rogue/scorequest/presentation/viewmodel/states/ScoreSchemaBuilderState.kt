package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.ComparisonRule
import com.rogue.scorequest.domain.model.DuplicableSchema
import com.rogue.scorequest.domain.model.ScoreFieldType
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.ScoreTerm
import com.rogue.scorequest.domain.model.WinnerMode

enum class BuilderStep { TYPE_CHOICE, FIELD_LIST, WINNER_ASSEMBLY, TEST_MODE }

data class ScoreSchemaBuilderState(
    val isLoading: Boolean = true,
    val gameName: String = "",
    val step: BuilderStep = BuilderStep.TYPE_CHOICE,
    val type: ScoreSchemaType? = null,
    val fields: List<ScoreFieldType> = emptyList(),
    val winnerMode: WinnerMode = WinnerMode.MANUAL,
    val formulaTerms: List<ScoreTerm> = emptyList(),
    val comparisonRule: ComparisonRule = ComparisonRule.HIGHEST_WINS,
    val duplicableSchemas: List<DuplicableSchema> = emptyList(),
    val isSaving: Boolean = false,
    val saved: Boolean = false
) {
    val eligibleFieldsForFormula: List<ScoreFieldType>
        get() = fields.filter { field ->
            when (field) {
                is ScoreFieldType.NumberField -> true
                is ScoreFieldType.BooleanField -> field.pointsIfChecked != null
                is ScoreFieldType.EnumField -> true
                is ScoreFieldType.MultiSelectField -> true
                is ScoreFieldType.TextField -> false
            }
        }

    val formulaPreviewText: String
        get() {
            if (formulaTerms.isEmpty()) return "Nenhum termo adicionado ainda"
            val fieldsByKey = fields.associateBy { it.key }
            val parts = formulaTerms.mapNotNull { term ->
                val label = fieldsByKey[term.fieldKey]?.label ?: return@mapNotNull null
                val weightText = when {
                    term.weight == 1.0 -> ""
                    term.weight == term.weight.toLong().toDouble() -> " ×${term.weight.toLong()}"
                    else -> " ×${term.weight}"
                }
                "$label$weightText"
            }
            return "Total = " + parts.joinToString(separator = " + ")
        }
}

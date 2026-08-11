package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.domain.model.GameScoreSchema
import com.rogue.scorequest.domain.model.MULTI_SELECT_VALUE_SEPARATOR
import com.rogue.scorequest.domain.model.ScoreFieldType
import kotlin.math.roundToInt

/**
 * Pure computation, no repository dependency: total = sum(fieldValue × termWeight)
 * for every term in the schema's formula (see product doc section 8.5).
 */
class CalculateScoreFormulaUseCase {
    operator fun invoke(schema: GameScoreSchema, fieldValues: Map<String, String>): Int? {
        val formula = schema.formula ?: return null
        val fieldsByKey = schema.fields.associateBy { it.key }

        val total = formula.terms.sumOf { term ->
            val field = fieldsByKey[term.fieldKey] ?: return@sumOf 0.0
            valueFor(field, fieldValues[term.fieldKey]) * term.weight
        }
        return total.roundToInt()
    }

    private fun valueFor(field: ScoreFieldType, rawValue: String?): Double = when (field) {
        is ScoreFieldType.NumberField -> (rawValue?.toIntOrNull() ?: field.default).toDouble()
        is ScoreFieldType.BooleanField ->
            if (rawValue == "true") (field.pointsIfChecked ?: 0).toDouble() else 0.0
        is ScoreFieldType.EnumField ->
            field.options.find { it.label == rawValue }?.points?.toDouble() ?: 0.0
        is ScoreFieldType.MultiSelectField -> {
            val selectedLabels = rawValue?.split(MULTI_SELECT_VALUE_SEPARATOR)?.toSet() ?: emptySet()
            field.options.filter { it.label in selectedLabels }.sumOf { it.points }.toDouble()
        }
        is ScoreFieldType.TextField -> 0.0
    }
}

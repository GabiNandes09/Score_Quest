package com.rogue.scorequest.data.seed

import com.rogue.scorequest.domain.model.ComparisonRule
import com.rogue.scorequest.domain.model.EnumOption
import com.rogue.scorequest.domain.model.ScoreFieldType
import com.rogue.scorequest.domain.model.ScoreFormula
import com.rogue.scorequest.domain.model.ScoreTerm

fun SeedScoreField.toDomain(): ScoreFieldType = when (type.uppercase()) {
    "NUMBER" -> ScoreFieldType.NumberField(
        key = key,
        label = label,
        default = default,
        min = min,
        max = max,
        allowNegative = allowNegative
    )
    "BOOLEAN" -> ScoreFieldType.BooleanField(
        key = key,
        label = label,
        pointsIfChecked = pointsIfChecked
    )
    "ENUM" -> ScoreFieldType.EnumField(
        key = key,
        label = label,
        options = options.orEmpty().map { it.toDomain() }
    )
    "MULTI_SELECT" -> ScoreFieldType.MultiSelectField(
        key = key,
        label = label,
        options = options.orEmpty().map { it.toDomain() }
    )
    "TEXT" -> ScoreFieldType.TextField(key = key, label = label)
    else -> throw IllegalArgumentException("Tipo de campo desconhecido: $type")
}

fun SeedEnumOption.toDomain() = EnumOption(label = label, points = points)

fun SeedFormula.toDomain() = ScoreFormula(
    terms = terms.map { ScoreTerm(fieldKey = it.fieldKey, weight = it.weight) },
    comparisonRule = ComparisonRule.valueOf(comparisonRule.uppercase())
)

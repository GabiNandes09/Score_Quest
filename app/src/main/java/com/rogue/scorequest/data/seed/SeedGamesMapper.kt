package com.rogue.scorequest.data.seed

import com.rogue.scorequest.domain.model.BoardGame
import com.rogue.scorequest.domain.model.ComparisonRule
import com.rogue.scorequest.domain.model.EnumOption
import com.rogue.scorequest.domain.model.GameScoreSchema
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

fun BoardGame.toSeedGame() = SeedGame(
    id = id,
    name = name,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    avgDurationMinutes = avgDurationMinutes,
    coverImageUrl = coverImageUrl,
    category = category,
    weight = weight,
    source = source.name
)

fun GameScoreSchema.toSeedScoreSchema() = SeedScoreSchema(
    gameId = gameId,
    type = type.name,
    winnerMode = winnerMode.name,
    fields = fields.map { it.toSeed() },
    formula = formula?.toSeed()
)

fun ScoreFieldType.toSeed(): SeedScoreField = when (this) {
    is ScoreFieldType.NumberField -> SeedScoreField(
        type = "NUMBER",
        key = key,
        label = label,
        default = default,
        min = min,
        max = max,
        allowNegative = allowNegative
    )
    is ScoreFieldType.BooleanField -> SeedScoreField(
        type = "BOOLEAN",
        key = key,
        label = label,
        pointsIfChecked = pointsIfChecked
    )
    is ScoreFieldType.EnumField -> SeedScoreField(
        type = "ENUM",
        key = key,
        label = label,
        options = options.map { it.toSeed() }
    )
    is ScoreFieldType.MultiSelectField -> SeedScoreField(
        type = "MULTI_SELECT",
        key = key,
        label = label,
        options = options.map { it.toSeed() }
    )
    is ScoreFieldType.TextField -> SeedScoreField(type = "TEXT", key = key, label = label)
}

fun EnumOption.toSeed() = SeedEnumOption(label = label, points = points)

fun ScoreFormula.toSeed() = SeedFormula(
    terms = terms.map { SeedTerm(fieldKey = it.fieldKey, weight = it.weight) },
    comparisonRule = comparisonRule.name
)

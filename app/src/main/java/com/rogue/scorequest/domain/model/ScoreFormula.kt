package com.rogue.scorequest.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ScoreFormula(
    val terms: List<ScoreTerm>,
    val comparisonRule: ComparisonRule // só relevante se winnerMode = AUTOMATIC
)

@Serializable
data class ScoreTerm(
    val fieldKey: String,
    val weight: Double = 1.0 // pode ser negativo (subtração); multiplica o valor do campo
)

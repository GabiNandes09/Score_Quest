package com.rogue.scorequest.data.seed

import kotlinx.serialization.Serializable

@Serializable
data class SeedGamesFile(
    val games: List<SeedGame> = emptyList(),
    val scoreSchemas: List<SeedScoreSchema> = emptyList()
)

@Serializable
data class SeedGame(
    val id: String,
    val name: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val avgDurationMinutes: Int,
    val coverImageUrl: String? = null,
    val category: String? = null,
    val weight: Double? = null,
    val source: String = "CURATED"
)

@Serializable
data class SeedScoreSchema(
    val gameId: String,
    val type: String,
    val winnerMode: String,
    val fields: List<SeedScoreField> = emptyList(),
    val formula: SeedFormula? = null
)

@Serializable
data class SeedScoreField(
    val type: String,
    val key: String,
    val label: String,
    val default: Int = 0,
    val min: Int? = null,
    val max: Int? = null,
    val allowNegative: Boolean = false,
    val pointsIfChecked: Int? = null,
    val options: List<SeedEnumOption>? = null
)

@Serializable
data class SeedEnumOption(
    val label: String,
    val points: Int = 0
)

@Serializable
data class SeedFormula(
    val terms: List<SeedTerm>,
    val comparisonRule: String
)

@Serializable
data class SeedTerm(
    val fieldKey: String,
    val weight: Double = 1.0
)

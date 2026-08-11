package com.rogue.scorequest.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Separator used to encode a MultiSelectField's chosen option labels into a
// single ScoreEntry.fieldValues string entry ("Ovos||Moedas"). "||" avoids
// clashing with commas that may appear inside an option label itself.
const val MULTI_SELECT_VALUE_SEPARATOR = "||"

@Serializable
sealed class ScoreFieldType {
    abstract val key: String
    abstract val label: String

    @Serializable
    @SerialName("NUMBER")
    data class NumberField(
        override val key: String,
        override val label: String,
        val default: Int = 0,
        val min: Int? = null,
        val max: Int? = null,
        val allowNegative: Boolean = false
    ) : ScoreFieldType()

    @Serializable
    @SerialName("BOOLEAN")
    data class BooleanField(
        override val key: String,
        override val label: String,
        val pointsIfChecked: Int? = null // null = só anotação, não pontua
    ) : ScoreFieldType()

    @Serializable
    @SerialName("ENUM")
    data class EnumField(
        override val key: String,
        override val label: String,
        val options: List<EnumOption>
    ) : ScoreFieldType()

    @Serializable
    @SerialName("MULTI_SELECT")
    data class MultiSelectField(
        override val key: String,
        override val label: String,
        val options: List<EnumOption>
    ) : ScoreFieldType()

    @Serializable
    @SerialName("TEXT")
    data class TextField(
        override val key: String,
        override val label: String
    ) : ScoreFieldType()
}

@Serializable
data class EnumOption(
    val label: String,
    val points: Int = 0 // 0 = opção sem impacto direto no total
)

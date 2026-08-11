package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class GameScoreSchema(
    val id: String,
    val gameId: String, // um schema por jogo (constraint de unicidade no banco)
    val type: ScoreSchemaType,
    val fields: List<ScoreFieldType> = emptyList(), // vazio quando type = SIMPLE
    val winnerMode: WinnerMode, // irrelevante quando type = SIMPLE (regra fixa: maior pontuação vence)
    val formula: ScoreFormula? = null, // preenchido só quando type = COMPOSITE e winnerMode = AUTOMATIC
    val createdByUserId: String,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

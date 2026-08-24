package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class GameScoreSchema(
    val id: String,
    val gameId: String, // um schema por jogo (constraint de unicidade no banco)
    val type: ScoreSchemaType,
    // vazio quando type = SIMPLE; quando type = RANKING, vazio (sem pontos) ou uma lista com
    // só o NumberField(key = RANKING_POINTS_FIELD_KEY) se a pontuação por jogador estiver ativada
    val fields: List<ScoreFieldType> = emptyList(),
    val winnerMode: WinnerMode, // irrelevante quando type = SIMPLE ou RANKING (RANKING: vencedor é sempre a posição 1)
    val formula: ScoreFormula? = null, // preenchido só quando type = COMPOSITE e winnerMode = AUTOMATIC
    val createdByUserId: String,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

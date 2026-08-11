package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class ScoreEntry(
    val sessionId: String,
    val playerId: String,
    val totalScore: Int?,      // input direto (Simples) ou calculado pela fórmula (Composta Automático); null se Manual/Sem vencedor sem total aplicável
    val isWinner: Boolean?,
    val fieldValues: Map<String, String>? = null, // preenchido só quando a partida usa um schema COMPOSITE; null para Pontuação Simples
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

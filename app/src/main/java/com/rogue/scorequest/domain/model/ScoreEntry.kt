package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class ScoreEntry(
    val sessionId: String,
    val playerId: String,
    val totalScore: Int?,      // input direto (Simples), calculado pela fórmula (Composta Automático) ou pontos por posição (Ranking, se ativado); null se não aplicável
    val isWinner: Boolean?,
    // COMPOSITE: valores por campo do schema. RANKING: RANKING_POSITION_FIELD_KEY (sempre) e
    // RANKING_POINTS_FIELD_KEY (se o schema tiver o campo de pontos). null para Pontuação Simples.
    val fieldValues: Map<String, String>? = null,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

// Helpers pra telas de leitura (SessionDetail, "Última jogatina" da Home) mostrarem a
// posição de partidas Ranking — mesma heurística usada em AddSessionViewModel pra
// restaurar o wizard em modo edição: a presença de RANKING_POSITION_FIELD_KEY já basta
// pra identificar uma sessão Ranking, sem precisar carregar o schema do jogo.
val ScoreEntry.rankingPosition: Int?
    get() = fieldValues?.get(RANKING_POSITION_FIELD_KEY)?.toIntOrNull()

val List<ScoreEntry>.isRankingSession: Boolean
    get() = any { it.rankingPosition != null }

fun List<ScoreEntry>.sortedByRankingPosition(): List<ScoreEntry> =
    sortedBy { it.rankingPosition ?: Int.MAX_VALUE }

// Estende a ideia de "posição" (1º/2º/...) pra Simples e Composta Automática, que têm
// pontuação numérica de verdade por trás — sem exigir carregar o schema do jogo aqui,
// só olhando o formato dos dados já salvos (mesmo espírito da heurística de rankingPosition
// acima). Composta Manual/Sem-vencedor não têm número pra ordenar por trás; ver
// hasNumberedPositionsForDisplay logo abaixo pra saber quando de fato numerar.
private fun List<ScoreEntry>.hasNumericScoreForDisplay(): Boolean {
    val hasFieldValues = any { !it.fieldValues.isNullOrEmpty() }
    return !hasFieldValues || any { it.totalScore != null }
}

/**
 * Ordena pra exibição: Ranking usa a posição já gravada; Simples/Composta Automática
 * (têm pontuação numérica) colocam o vencedor primeiro e o resto por pontuação
 * decrescente; Composta Manual só sobe o vencedor pro topo, sem reordenar o resto (não
 * há número pra ordenar por trás); cooperativo/sem vencedor mantém a ordem original.
 */
fun List<ScoreEntry>.orderedForDisplay(): List<ScoreEntry> {
    if (isRankingSession) return sortedByRankingPosition()
    if (none { it.isWinner == true }) return this
    return if (hasNumericScoreForDisplay()) {
        sortedWith(compareByDescending<ScoreEntry> { it.isWinner == true }.thenByDescending { it.totalScore ?: Int.MIN_VALUE })
    } else {
        sortedByDescending { it.isWinner == true }
    }
}

/** Se `orderedForDisplay()` deve vir acompanhado de badges "1º"/"2º"/etc. */
fun List<ScoreEntry>.hasNumberedPositionsForDisplay(): Boolean =
    isRankingSession || (any { it.isWinner == true } && hasNumericScoreForDisplay())

package com.rogue.scorequest.domain.model

enum class HomeWidget(val label: String) {
    ACTIVITY_HEATMAP("Atividade recente"),
    TOP_GAMES("Ranking dos mais jogados"),
    TOP_PLAYERS("Mais vitórias"),
    TIMELINE("Partidas por mês"),
    DURATION_HISTOGRAM("Duração das partidas")
}

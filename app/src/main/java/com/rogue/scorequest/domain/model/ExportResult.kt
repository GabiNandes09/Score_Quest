package com.rogue.scorequest.domain.model

data class ExportResult(
    val json: String,
    val gamesCount: Int,
    val schemasCount: Int
)

package com.rogue.scorequest.domain.model

data class ImportResult(
    val gamesAdded: Int = 0,
    val gamesUpdated: Int = 0,
    val schemasImported: Int = 0,
    val errors: List<String> = emptyList()
)

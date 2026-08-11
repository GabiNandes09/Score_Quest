package com.rogue.scorequest.domain.model

data class DurationBucket(
    val label: String, // "0-30min", "30-60min", "1-2h", "2h+"
    val sessionCount: Int
)

package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

interface Auditable {
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
    val deletedAt: LocalDateTime?
}

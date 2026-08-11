package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class UserLibraryEntry(
    val gameId: String,
    val status: LibraryStatus,
    val played: Boolean = false,
    val lentTo: String? = null,
    val rating: Int? = null,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

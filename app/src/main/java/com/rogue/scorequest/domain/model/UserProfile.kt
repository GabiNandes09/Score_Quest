package com.rogue.scorequest.domain.model

import java.time.LocalDateTime

data class UserProfile(
    val id: String = LOCAL_PROFILE_ID,
    val displayName: String,
    val bio: String? = null,
    val avatarUri: String? = null,
    val updatedAt: LocalDateTime
) {
    companion object {
        const val LOCAL_PROFILE_ID = "local"
    }
}

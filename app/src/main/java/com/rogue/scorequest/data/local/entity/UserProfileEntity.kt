package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rogue.scorequest.domain.model.UserProfile
import com.rogue.scorequest.utils.toEpochMillis
import com.rogue.scorequest.utils.toLocalDateTime

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    val bio: String?,
    @ColumnInfo(name = "avatar_uri") val avatarUri: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

fun UserProfileEntity.toDomain() = UserProfile(
    id = id,
    displayName = displayName,
    bio = bio,
    avatarUri = avatarUri,
    updatedAt = updatedAt.toLocalDateTime()
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = id,
    displayName = displayName,
    bio = bio,
    avatarUri = avatarUri,
    updatedAt = updatedAt.toEpochMillis()
)

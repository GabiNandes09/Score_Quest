package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.UserLibraryEntry
import com.rogue.scorequest.utils.toEpochMillis
import com.rogue.scorequest.utils.toLocalDateTime

@Entity(
    tableName = "user_library_entry",
    foreignKeys = [
        ForeignKey(
            entity = BoardGameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"]
        )
    ]
)
data class UserLibraryEntryEntity(
    @PrimaryKey @ColumnInfo(name = "game_id") val gameId: String,
    val status: LibraryStatus,
    val played: Boolean,
    @ColumnInfo(name = "lent_to") val lentTo: String?,
    val rating: Int?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?
)

fun UserLibraryEntryEntity.toDomain() = UserLibraryEntry(
    gameId = gameId,
    status = status,
    played = played,
    lentTo = lentTo,
    rating = rating,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun UserLibraryEntry.toEntity() = UserLibraryEntryEntity(
    gameId = gameId,
    status = status,
    played = played,
    lentTo = lentTo,
    rating = rating,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

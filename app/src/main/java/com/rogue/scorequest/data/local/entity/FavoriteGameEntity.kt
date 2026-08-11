package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_game",
    foreignKeys = [
        ForeignKey(
            entity = BoardGameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"]
        )
    ],
    indices = [Index("game_id", unique = true)]
)
data class FavoriteGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "game_id") val gameId: String,
    val position: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

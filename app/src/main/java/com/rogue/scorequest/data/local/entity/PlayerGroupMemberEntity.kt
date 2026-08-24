package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.Relation
import com.rogue.scorequest.domain.model.PlayerGroup

@Entity(
    tableName = "player_group_member",
    primaryKeys = ["group_id", "player_id"],
    foreignKeys = [
        ForeignKey(
            entity = PlayerGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["group_id"]
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"]
        )
    ],
    indices = [Index("group_id"), Index("player_id")]
)
data class PlayerGroupMemberEntity(
    @ColumnInfo(name = "group_id") val groupId: String,
    @ColumnInfo(name = "player_id") val playerId: String
)

data class PlayerGroupWithMembersEntity(
    @Embedded val group: PlayerGroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlayerGroupMemberEntity::class,
            parentColumn = "group_id",
            entityColumn = "player_id"
        )
    )
    val members: List<PlayerEntity>
)

fun PlayerGroupWithMembersEntity.toDomain(): PlayerGroup = group.toDomain(members.map { it.id })

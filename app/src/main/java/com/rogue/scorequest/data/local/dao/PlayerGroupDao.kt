package com.rogue.scorequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rogue.scorequest.data.local.entity.PlayerGroupEntity
import com.rogue.scorequest.data.local.entity.PlayerGroupMemberEntity
import com.rogue.scorequest.data.local.entity.PlayerGroupWithMembersEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerGroupDao {

    @Insert
    suspend fun insert(group: PlayerGroupEntity)

    @Update
    suspend fun update(group: PlayerGroupEntity)

    @Query("UPDATE player_group SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Insert
    suspend fun insertMembers(members: List<PlayerGroupMemberEntity>)

    @Query("DELETE FROM player_group_member WHERE group_id = :groupId")
    suspend fun deleteMembersForGroup(groupId: String)

    @Transaction
    suspend fun replaceMembers(groupId: String, playerIds: List<String>) {
        deleteMembersForGroup(groupId)
        insertMembers(playerIds.map { PlayerGroupMemberEntity(groupId = groupId, playerId = it) })
    }

    @Transaction
    @Query("SELECT * FROM player_group WHERE deleted_at IS NULL ORDER BY name ASC")
    fun getAll(): Flow<List<PlayerGroupWithMembersEntity>>

    @Transaction
    @Query("SELECT * FROM player_group WHERE id = :id AND deleted_at IS NULL")
    fun getById(id: String): Flow<PlayerGroupWithMembersEntity?>
}

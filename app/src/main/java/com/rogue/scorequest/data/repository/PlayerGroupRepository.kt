package com.rogue.scorequest.data.repository

import com.rogue.scorequest.data.local.dao.PlayerGroupDao
import com.rogue.scorequest.data.local.entity.toDomain
import com.rogue.scorequest.data.local.entity.toEntity
import com.rogue.scorequest.domain.model.PlayerGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlayerGroupRepository(
    private val playerGroupDao: PlayerGroupDao
) {

    fun getGroups(): Flow<List<PlayerGroup>> =
        playerGroupDao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getGroupsOnce(): List<PlayerGroup> = getGroups().first()

    fun getGroup(id: String): Flow<PlayerGroup?> =
        playerGroupDao.getById(id).map { it?.toDomain() }

    suspend fun createGroup(group: PlayerGroup) {
        playerGroupDao.insert(group.toEntity())
        playerGroupDao.replaceMembers(group.id, group.memberIds)
    }

    suspend fun updateGroup(group: PlayerGroup) {
        playerGroupDao.update(group.toEntity())
        playerGroupDao.replaceMembers(group.id, group.memberIds)
    }

    suspend fun deleteGroup(id: String, deletedAt: Long) = playerGroupDao.softDelete(id, deletedAt)
}

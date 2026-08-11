package com.rogue.scorequest.data.repository

import com.rogue.scorequest.data.local.dao.FavoriteGameDao
import com.rogue.scorequest.data.local.dao.UserProfileDao
import com.rogue.scorequest.data.local.entity.FavoriteGameEntity
import com.rogue.scorequest.data.local.entity.toDomain
import com.rogue.scorequest.data.local.entity.toEntity
import com.rogue.scorequest.domain.model.BoardGame
import com.rogue.scorequest.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val favoriteGameDao: FavoriteGameDao
) {

    fun getProfile(): Flow<UserProfile?> =
        userProfileDao.getProfile().map { it?.toDomain() }

    suspend fun saveProfile(profile: UserProfile) = userProfileDao.upsert(profile.toEntity())

    fun getFavoriteGames(): Flow<List<BoardGame>> =
        favoriteGameDao.getFavoriteGames().map { list -> list.map { it.toDomain() } }

    suspend fun getFavoriteCount(): Int = favoriteGameDao.getCount()

    suspend fun getOldestFavoriteGameId(): String? = favoriteGameDao.getOldest()?.gameId

    suspend fun addFavorite(gameId: String) {
        favoriteGameDao.deleteByGameId(gameId)
        val position = favoriteGameDao.getCount()
        favoriteGameDao.insert(
            FavoriteGameEntity(gameId = gameId, position = position, createdAt = System.currentTimeMillis())
        )
    }

    suspend fun removeFavorite(gameId: String) = favoriteGameDao.deleteByGameId(gameId)
}

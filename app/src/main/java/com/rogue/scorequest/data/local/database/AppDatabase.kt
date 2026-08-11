package com.rogue.scorequest.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rogue.scorequest.data.local.dao.BoardGameDao
import com.rogue.scorequest.data.local.dao.FavoriteGameDao
import com.rogue.scorequest.data.local.dao.GameSessionDao
import com.rogue.scorequest.data.local.dao.PlayerDao
import com.rogue.scorequest.data.local.dao.ScoreEntryDao
import com.rogue.scorequest.data.local.dao.UserLibraryEntryDao
import com.rogue.scorequest.data.local.dao.UserProfileDao
import com.rogue.scorequest.data.local.entity.BoardGameEntity
import com.rogue.scorequest.data.local.entity.Converters
import com.rogue.scorequest.data.local.entity.FavoriteGameEntity
import com.rogue.scorequest.data.local.entity.GameSessionEntity
import com.rogue.scorequest.data.local.entity.PlayerEntity
import com.rogue.scorequest.data.local.entity.ScoreEntryEntity
import com.rogue.scorequest.data.local.entity.UserLibraryEntryEntity
import com.rogue.scorequest.data.local.entity.UserProfileEntity

@Database(
    entities = [
        BoardGameEntity::class,
        UserLibraryEntryEntity::class,
        PlayerEntity::class,
        GameSessionEntity::class,
        ScoreEntryEntity::class,
        UserProfileEntity::class,
        FavoriteGameEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boardGameDao(): BoardGameDao
    abstract fun userLibraryEntryDao(): UserLibraryEntryDao
    abstract fun playerDao(): PlayerDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun scoreEntryDao(): ScoreEntryDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun favoriteGameDao(): FavoriteGameDao
}

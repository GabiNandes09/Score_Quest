package com.rogue.scorequest.di

import androidx.room.Room
import com.rogue.scorequest.data.local.database.AppDatabase
import com.rogue.scorequest.data.local.database.MIGRATION_1_2
import com.rogue.scorequest.data.local.database.MIGRATION_2_3
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(), AppDatabase::class.java, "scorequest.db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }

    single { get<AppDatabase>().boardGameDao() }
    single { get<AppDatabase>().userLibraryEntryDao() }
    single { get<AppDatabase>().playerDao() }
    single { get<AppDatabase>().gameSessionDao() }
    single { get<AppDatabase>().scoreEntryDao() }
    single { get<AppDatabase>().userProfileDao() }
    single { get<AppDatabase>().favoriteGameDao() }
    single { get<AppDatabase>().gameScoreSchemaDao() }
    single { get<AppDatabase>().activeTimerDao() }
    single { get<AppDatabase>().playerGroupDao() }
}

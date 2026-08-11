package com.rogue.scorequest.di

import com.rogue.scorequest.data.local.ThemePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesModule = module {
    single { ThemePreferences(androidContext()) }
}

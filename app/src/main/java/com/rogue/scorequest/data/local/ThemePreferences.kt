package com.rogue.scorequest.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_THEME_KEY] ?: true
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_THEME_KEY] = enabled
        }
    }

    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }
}

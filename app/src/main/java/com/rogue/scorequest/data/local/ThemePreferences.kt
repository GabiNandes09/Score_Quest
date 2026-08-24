package com.rogue.scorequest.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rogue.scorequest.domain.model.HomeWidget
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

    // Guarda só os widgets ESCONDIDOS (nome do enum) — ausência da chave = tudo visível por
    // padrão, sem precisar popular o Set inteiro na primeira leitura.
    val visibleHomeWidgets: Flow<Set<HomeWidget>> = context.dataStore.data.map { prefs ->
        val hidden = prefs[HIDDEN_HOME_WIDGETS_KEY] ?: emptySet()
        HomeWidget.entries.filterNot { it.name in hidden }.toSet()
    }

    suspend fun setHomeWidgetVisible(widget: HomeWidget, visible: Boolean) {
        context.dataStore.edit { prefs ->
            val hidden = prefs[HIDDEN_HOME_WIDGETS_KEY] ?: emptySet()
            prefs[HIDDEN_HOME_WIDGETS_KEY] = if (visible) hidden - widget.name else hidden + widget.name
        }
    }

    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        private val HIDDEN_HOME_WIDGETS_KEY = stringSetPreferencesKey("hidden_home_widgets")
    }
}

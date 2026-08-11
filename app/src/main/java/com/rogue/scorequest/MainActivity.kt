package com.rogue.scorequest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rogue.scorequest.data.local.ThemePreferences
import com.rogue.scorequest.presentation.navigation.AppNavigation
import com.rogue.scorequest.ui.theme.ScoreQuestTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themePreferences = koinInject<ThemePreferences>()
            val isDarkTheme by themePreferences.isDarkTheme.collectAsState(initial = true)

            ScoreQuestTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}

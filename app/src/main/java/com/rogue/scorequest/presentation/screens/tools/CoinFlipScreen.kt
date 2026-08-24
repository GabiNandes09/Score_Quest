package com.rogue.scorequest.presentation.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.presentation.components.rememberSpinTicker
import com.rogue.scorequest.ui.theme.Gold
import kotlinx.coroutines.launch

private val COIN_OPTIONS = listOf("Cara", "Coroa")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinFlipScreen(onBackClick: () -> Unit) {
    val ticker = rememberSpinTicker(randomValue = { COIN_OPTIONS.random() })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moeda") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = ticker.current ?: "?",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Gold
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { scope.launch { ticker.spin(COIN_OPTIONS.random()) } },
                enabled = !ticker.isSpinning
            ) {
                Text("Jogar a moeda")
            }
        }
    }
}

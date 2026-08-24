package com.rogue.scorequest.presentation.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.presentation.components.EditableTextList
import com.rogue.scorequest.ui.theme.Gold
import kotlinx.coroutines.delay

private const val MIN_SECONDS = 10
private const val MAX_SECONDS = 300
private const val SECONDS_STEP = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnTimerScreen(onBackClick: () -> Unit) {
    var players by remember { mutableStateOf(listOf<String>()) }
    var secondsPerTurn by remember { mutableIntStateOf(60) }
    var hasStarted by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var remainingSeconds by remember { mutableIntStateOf(secondsPerTurn) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            remainingSeconds -= 1
            if (remainingSeconds <= 0) {
                currentIndex = (currentIndex + 1) % players.size
                remainingSeconds = secondsPerTurn
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cronômetro por turno") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!hasStarted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Jogadores (${players.size})",
                    style = MaterialTheme.typography.labelLarge
                )
                EditableTextList(
                    items = players,
                    onAdd = { value -> if (value.isNotBlank()) players = players + value.trim() },
                    onRemove = { index -> players = players.filterIndexed { i, _ -> i != index } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Nome do jogador"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { secondsPerTurn = (secondsPerTurn - SECONDS_STEP).coerceAtLeast(MIN_SECONDS) }) {
                        Icon(Icons.Filled.Remove, contentDescription = null)
                    }
                    Text(
                        text = "$secondsPerTurn s por turno",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = { secondsPerTurn = (secondsPerTurn + SECONDS_STEP).coerceAtMost(MAX_SECONDS) }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    }
                }

                Button(
                    onClick = {
                        currentIndex = 0
                        remainingSeconds = secondsPerTurn
                        hasStarted = true
                        isRunning = true
                    },
                    enabled = players.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Vez de",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = players[currentIndex],
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )

                Text(
                    text = "$remainingSeconds s",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                LinearProgressIndicator(
                    progress = { remainingSeconds.toFloat() / secondsPerTurn.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(if (isRunning) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null)
                        Text(if (isRunning) "Pausar" else "Retomar")
                    }
                    OutlinedButton(
                        onClick = {
                            currentIndex = (currentIndex + 1) % players.size
                            remainingSeconds = secondsPerTurn
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Próximo")
                    }
                }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        hasStarted = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Encerrar")
                }
            }
        }
    }
}

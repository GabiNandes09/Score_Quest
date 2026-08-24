package com.rogue.scorequest.presentation.screens.livematch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.TimerStatus
import com.rogue.scorequest.presentation.viewmodel.LiveMatchViewModel
import com.rogue.scorequest.ui.theme.Gold
import com.rogue.scorequest.utils.formatElapsed
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMatchScreen(
    gameId: String,
    onBack: () -> Unit,
    onFinished: (gameId: String, minutes: Int) -> Unit,
    onGoToConflicting: (gameId: String) -> Unit,
    viewModel: LiveMatchViewModel = koinViewModel(parameters = { parametersOf(gameId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finishedDurationMinutes) {
        state.finishedDurationMinutes?.let { minutes -> onFinished(gameId, minutes) }
    }
    LaunchedEffect(state.cancelled) {
        if (state.cancelled) onBack()
    }

    val conflicting = state.conflictingTimer
    if (conflicting != null) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Partida ao vivo") }) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Já existe uma partida em andamento: ${conflicting.gameName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = { onGoToConflicting(conflicting.gameId) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ir para a partida em andamento")
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Voltar")
                }
            }
        }
        return
    }

    val timer = state.timer

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timer?.status) {
        while (timer?.status == TimerStatus.RUNNING) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    if (state.showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissCancelConfirmation,
            title = { Text("Cancelar partida?") },
            text = { Text("O tempo registrado será perdido e nenhuma partida será salva.") },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmCancel) { Text("Cancelar partida") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissCancelConfirmation) { Text("Voltar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(timer?.gameName ?: "Partida ao vivo") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (timer != null) {
                Text(
                    text = formatElapsed(timer.elapsedMillis(nowMillis)),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (timer.status == TimerStatus.PAUSED) MaterialTheme.colorScheme.onSurfaceVariant else Gold
                )
                Text(
                    text = if (timer.status == TimerStatus.PAUSED) "Em pausa" else "Em andamento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (timer.status == TimerStatus.RUNNING) {
                        OutlinedButton(onClick = viewModel::onPause, modifier = Modifier.weight(1f)) {
                            Text("Suspender")
                        }
                    } else {
                        OutlinedButton(onClick = viewModel::onResume, modifier = Modifier.weight(1f)) {
                            Text("Retomar")
                        }
                    }
                    OutlinedButton(onClick = viewModel::onRequestCancel, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                }
                Button(
                    onClick = viewModel::onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Finalizar partida")
                }
            }
        }
    }
}

package com.rogue.scorequest.presentation.screens.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.presentation.components.CompositeFieldInputForm
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompositeScoringStep(
    viewModel: AddSessionViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val schema by viewModel.schema.collectAsStateWithLifecycle()
    val participants = remember(players, state.selectedPlayerIds) {
        players.filter { it.id in state.selectedPlayerIds }
    }
    var currentIndex by remember { mutableStateOf(0) }
    val currentPlayer = participants.getOrNull(currentIndex)
    val fields = schema?.fields.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentPlayer != null) {
                            "Pontuação — ${currentPlayer.nickname} (${currentIndex + 1}/${participants.size})"
                        } else {
                            "Pontuação"
                        }
                    )
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (currentPlayer != null) {
                    CompositeFieldInputForm(
                        fields = fields,
                        values = state.compositeFieldValues[currentPlayer.id] ?: emptyMap(),
                        onValueChange = { key, value -> viewModel.onCompositeFieldChange(currentPlayer.id, key, value) }
                    )
                } else {
                    Text(text = "Nenhum jogador selecionado.", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = { currentIndex-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Jogador anterior")
                    }
                }
                Button(
                    onClick = {
                        if (currentIndex < participants.lastIndex) currentIndex++ else onNext()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (currentIndex < participants.lastIndex) "Próximo jogador" else "Continuar")
                }
            }
        }
    }
}

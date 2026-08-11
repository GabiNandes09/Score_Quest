package com.rogue.scorequest.presentation.screens.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringStep(
    viewModel: AddSessionViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val participants = players.filter { it.id in state.selectedPlayerIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pontuação") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(participants) { player ->
                    ScoreRow(
                        player = player,
                        score = state.scores[player.id]?.totalScore.orEmpty(),
                        isWinner = state.scores[player.id]?.isWinner == true,
                        onScoreChange = { viewModel.onScoreChange(player.id, it) },
                        onWinnerToggle = { viewModel.onWinnerToggled(player.id) }
                    )
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Próximo")
            }
        }
    }
}

@Composable
private fun ScoreRow(
    player: Player,
    score: String,
    isWinner: Boolean,
    onScoreChange: (String) -> Unit,
    onWinnerToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = player.nickname,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = score,
                onValueChange = onScoreChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Pontos") }
            )
            Column {
                Text(text = "Venceu", style = MaterialTheme.typography.labelSmall)
                Checkbox(checked = isWinner, onCheckedChange = { onWinnerToggle() })
            }
        }
    }
}

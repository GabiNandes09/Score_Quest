package com.rogue.scorequest.presentation.screens.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.presentation.components.GroupChipRow
import com.rogue.scorequest.presentation.components.PlayerRow
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersStep(
    viewModel: AddSessionViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val schema by viewModel.schema.collectAsStateWithLifecycle()
    var newNickname by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jogadores") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (groups.isNotEmpty()) {
                Text(
                    text = "Começar de um grupo",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                GroupChipRow(
                    groups = groups,
                    selectedGroupId = state.selectedGroupId,
                    onGroupClick = { group -> viewModel.onGroupSelected(group.id) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newNickname,
                    onValueChange = { newNickname = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Novo jogador") },
                    singleLine = true
                )
                OutlinedButton(onClick = {
                    viewModel.createAndAddPlayer(newNickname)
                    newNickname = ""
                }) {
                    Text("+ Criar")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(players) { player ->
                    PlayerRow(
                        player = player,
                        selected = player.id in state.selectedPlayerIds,
                        onToggle = { viewModel.onPlayerToggled(player.id) }
                    )
                }
            }

            if (schema?.type == ScoreSchemaType.COMPOSITE) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pontuação simples (só total e vencedor)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = state.useSimpleEntry,
                            onCheckedChange = { viewModel.onToggleSimpleEntry() }
                        )
                    }
                }
            }

            Button(
                onClick = onNext,
                enabled = state.canProceedFromPlayersStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Próximo")
            }
        }
    }
}

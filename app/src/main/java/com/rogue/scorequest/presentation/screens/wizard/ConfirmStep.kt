package com.rogue.scorequest.presentation.screens.wizard

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.WinnerMode
import com.rogue.scorequest.presentation.components.PlayerIdentityRow
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmStep(
    viewModel: AddSessionViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val schema by viewModel.schema.collectAsStateWithLifecycle()
    val participants = players.filter { it.id in state.selectedPlayerIds }
    val isComposite = schema?.type == ScoreSchemaType.COMPOSITE

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    LaunchedEffect(state.compositeFieldValues, schema) {
        if (isComposite && schema?.winnerMode == WinnerMode.AUTOMATIC) {
            viewModel.resolveAutomaticWinnerIfNeeded()
        }
    }

    val canSave = !state.isSaving && when {
        isComposite && schema?.winnerMode == WinnerMode.MANUAL -> state.manualWinnerId != null
        isComposite && schema?.winnerMode == WinnerMode.AUTOMATIC -> state.pendingTieCandidateIds.isEmpty()
        else -> true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmação") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = state.selectedGameName, style = MaterialTheme.typography.headlineSmall)
            Text(text = "Data: ${state.date.format(dateFormatter)}")
            Text(text = "Duração: ${state.durationMinutes} min")
            if (state.variantOrExpansion.isNotBlank()) {
                Text(text = "Variante: ${state.variantOrExpansion}")
            }

            Text(text = "Jogadores e pontuação", style = MaterialTheme.typography.titleMedium)

            if (isComposite) {
                when (schema?.winnerMode) {
                    WinnerMode.MANUAL -> ManualWinnerList(
                        participants = participants,
                        selectedId = state.manualWinnerId,
                        onSelected = viewModel::onManualWinnerSelected
                    )
                    WinnerMode.AUTOMATIC -> AutomaticWinnerList(
                        participants = participants,
                        winnerIds = state.automaticWinnerIds,
                        totalFor = viewModel::calculateTotalFor
                    )
                    else -> participants.forEach { player ->
                        PlayerIdentityRow(name = player.nickname, isWinner = false)
                    }
                }
            } else {
                participants.forEach { player ->
                    val entry = state.scores[player.id]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PlayerIdentityRow(name = player.nickname, isWinner = entry?.isWinner == true)
                        Text(text = entry?.totalScore.orEmpty())
                    }
                }
            }

            Button(
                onClick = viewModel::save,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar partida")
            }
        }
    }

    if (isComposite && schema?.winnerMode == WinnerMode.AUTOMATIC && state.pendingTieCandidateIds.isNotEmpty()) {
        TieBreakDialog(
            candidateNames = state.pendingTieCandidateIds.mapNotNull { id -> participants.find { it.id == id }?.nickname },
            candidates = participants.filter { it.id in state.pendingTieCandidateIds },
            onConsiderTie = viewModel::resolveTieAsDoubleWinner,
            onPickManually = viewModel::resolveTieManually
        )
    }
}

@Composable
private fun ManualWinnerList(
    participants: List<Player>,
    selectedId: String?,
    onSelected: (String) -> Unit
) {
    participants.forEach { player ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelected(player.id) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerIdentityRow(name = player.nickname, isWinner = player.id == selectedId)
            RadioButton(selected = player.id == selectedId, onClick = { onSelected(player.id) })
        }
    }
}

@Composable
private fun AutomaticWinnerList(
    participants: List<Player>,
    winnerIds: Set<String>,
    totalFor: (String) -> Int?
) {
    participants.forEach { player ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlayerIdentityRow(name = player.nickname, isWinner = player.id in winnerIds)
            Text(text = "${totalFor(player.id) ?: "-"}")
        }
    }
}

@Composable
private fun TieBreakDialog(
    candidateNames: List<String>,
    candidates: List<Player>,
    onConsiderTie: () -> Unit,
    onPickManually: (String) -> Unit
) {
    var showManualPicker by remember { mutableStateOf(false) }

    if (!showManualPicker) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Empate!") },
            text = { Text("${candidateNames.joinToString(" e ")} empataram. O que você quer fazer?") },
            confirmButton = {
                TextButton(onClick = onConsiderTie) { Text("Considerar empate") }
            },
            dismissButton = {
                TextButton(onClick = { showManualPicker = true }) { Text("Escolher manualmente") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { showManualPicker = false },
            title = { Text("Quem venceu?") },
            text = {
                Column {
                    candidates.forEach { player ->
                        Text(
                            text = player.nickname,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPickManually(player.id) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showManualPicker = false }) { Text("Voltar") } }
        )
    }
}

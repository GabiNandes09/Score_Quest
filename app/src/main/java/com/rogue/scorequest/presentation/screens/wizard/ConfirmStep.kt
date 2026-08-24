package com.rogue.scorequest.presentation.screens.wizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.ComparisonRule
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.WinnerMode
import com.rogue.scorequest.presentation.components.PlayerIdentityRow
import com.rogue.scorequest.presentation.components.PositionBadge
import com.rogue.scorequest.presentation.components.StatIconItem
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel
import com.rogue.scorequest.ui.theme.Gold
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
    val isComposite = schema?.type == ScoreSchemaType.COMPOSITE && !state.useSimpleEntry
    val isRanking = schema?.type == ScoreSchemaType.RANKING
    val hasRankingPoints = schema?.fields?.isNotEmpty() == true

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
        isRanking -> state.rankingOrder.size == participants.size
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = state.selectedGameName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatIconItem(icon = Icons.Filled.DateRange, value = state.date.format(dateFormatter))
                        StatIconItem(icon = Icons.Filled.Timer, value = "${state.durationMinutes} min")
                    }
                    if (state.variantOrExpansion.isNotBlank()) {
                        Text(
                            text = "Variante: ${state.variantOrExpansion}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Jogadores e pontuação", style = MaterialTheme.typography.titleMedium, color = Gold)

                    when {
                        isComposite -> {
                            when (schema?.winnerMode) {
                                WinnerMode.MANUAL -> ManualWinnerList(
                                    participants = participants,
                                    selectedId = state.manualWinnerId,
                                    onSelected = viewModel::onManualWinnerSelected
                                )
                                WinnerMode.AUTOMATIC -> AutomaticWinnerList(
                                    participants = participants,
                                    winnerIds = state.automaticWinnerIds,
                                    totalFor = viewModel::calculateTotalFor,
                                    comparisonRule = schema?.formula?.comparisonRule ?: ComparisonRule.HIGHEST_WINS
                                )
                                else -> participants.forEach { player ->
                                    PlayerIdentityRow(name = player.nickname, isWinner = false)
                                }
                            }
                        }
                        isRanking -> RankingConfirmList(
                            order = state.rankingOrder,
                            participants = participants,
                            points = state.rankingPoints,
                            showPoints = hasRankingPoints
                        )
                        else -> {
                            val ordered = participants.sortedWith(
                                compareByDescending<Player> { state.scores[it.id]?.isWinner == true }
                                    .thenByDescending { state.scores[it.id]?.totalScore?.toIntOrNull() ?: Int.MIN_VALUE }
                            )
                            ordered.forEachIndexed { index, player ->
                                val entry = state.scores[player.id]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PositionBadge(position = index + 1, modifier = Modifier.padding(end = 8.dp))
                                        PlayerIdentityRow(name = player.nickname, isWinner = entry?.isWinner == true)
                                    }
                                    Text(text = entry?.totalScore.orEmpty())
                                }
                            }
                        }
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
    totalFor: (String) -> Int?,
    comparisonRule: ComparisonRule
) {
    val byTotal = compareByDescending<Player> { it.id in winnerIds }.let { base ->
        if (comparisonRule == ComparisonRule.LOWEST_WINS) {
            base.thenBy { totalFor(it.id) ?: Int.MAX_VALUE }
        } else {
            base.thenByDescending { totalFor(it.id) ?: Int.MIN_VALUE }
        }
    }
    participants.sortedWith(byTotal).forEachIndexed { index, player ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PositionBadge(position = index + 1, modifier = Modifier.padding(end = 8.dp))
                PlayerIdentityRow(name = player.nickname, isWinner = player.id in winnerIds)
            }
            Text(text = "${totalFor(player.id) ?: "-"}")
        }
    }
}

@Composable
private fun RankingConfirmList(
    order: List<String>,
    participants: List<Player>,
    points: Map<String, String>,
    showPoints: Boolean
) {
    order.forEachIndexed { index, playerId ->
        val player = participants.find { it.id == playerId } ?: return@forEachIndexed
        val position = index + 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PositionBadge(position = position, modifier = Modifier.padding(end = 8.dp))
                PlayerIdentityRow(name = player.nickname, isWinner = position == 1)
            }
            if (showPoints) {
                Text(text = points[playerId].orEmpty())
            }
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

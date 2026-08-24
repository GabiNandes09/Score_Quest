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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.presentation.components.GroupChipRow
import com.rogue.scorequest.presentation.components.PlayerMultiSelectSection
import com.rogue.scorequest.presentation.viewmodel.ShuffleTeamsViewModel
import com.rogue.scorequest.ui.theme.Gold
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuffleTeamsScreen(
    onBackClick: () -> Unit,
    viewModel: ShuffleTeamsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val selectedPlayers = players.filter { it.id in state.selectedPlayerIds }
    var teams by remember { mutableStateOf<List<List<Player>>>(emptyList()) }
    val canShuffle = selectedPlayers.size >= state.numberOfTeams

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sorteio de equipes") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (groups.isNotEmpty()) {
                GroupChipRow(
                    groups = groups,
                    selectedGroupId = state.selectedGroupId,
                    onGroupClick = viewModel::onGroupSelected
                )
            }

            PlayerMultiSelectSection(
                players = players,
                selectedIds = state.selectedPlayerIds,
                onToggle = viewModel::onPlayerToggled,
                modifier = Modifier.fillMaxWidth(),
                listHeight = 280.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.onNumberOfTeamsChanged(-1) }) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                }
                Text(
                    text = "${state.numberOfTeams} times",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { viewModel.onNumberOfTeamsChanged(1) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            if (teams.isNotEmpty()) {
                teams.forEachIndexed { index, team ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Time ${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                            team.forEach { player ->
                                Text(text = player.nickname, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val shuffled = selectedPlayers.shuffled()
                    val n = state.numberOfTeams
                    teams = List(n) { teamIndex -> shuffled.filterIndexed { i, _ -> i % n == teamIndex } }
                },
                enabled = canShuffle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sortear equipes")
            }
        }
    }
}

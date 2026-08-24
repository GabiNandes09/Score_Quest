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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.presentation.components.EditableTextList
import com.rogue.scorequest.presentation.components.GroupChipRow
import com.rogue.scorequest.presentation.components.PlayerMultiSelectSection
import com.rogue.scorequest.presentation.components.rememberSpinTicker
import com.rogue.scorequest.presentation.viewmodel.AssignRolesViewModel
import com.rogue.scorequest.ui.theme.Gold
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignRolesScreen(
    onBackClick: () -> Unit,
    viewModel: AssignRolesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val selectedPlayers = players.filter { it.id in state.selectedPlayerIds }
    val ticker = rememberSpinTicker(state.roles, randomValue = { state.roles.shuffled() })
    val scope = rememberCoroutineScope()

    val countsMatch = state.roles.size == selectedPlayers.size
    val canShuffle = selectedPlayers.size >= 2 && countsMatch

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sorteio de papéis") },
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
                listHeight = 240.dp
            )

            Text(
                text = "Papéis (${state.roles.size} de ${selectedPlayers.size} jogadores)",
                style = MaterialTheme.typography.labelLarge
            )

            EditableTextList(
                items = state.roles,
                onAdd = viewModel::onAddRole,
                onRemove = viewModel::onRemoveRole,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Novo papel"
            )

            if (!countsMatch && state.roles.isNotEmpty() && selectedPlayers.isNotEmpty()) {
                Text(
                    text = "O número de papéis precisa ser igual ao número de jogadores selecionados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            ticker.current?.let { shuffledRoles ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedPlayers.zip(shuffledRoles).forEach { (player, role) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = player.nickname, fontWeight = FontWeight.Bold)
                                Text(text = role, color = Gold)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { scope.launch { ticker.spin(state.roles.shuffled()) } },
                enabled = !ticker.isSpinning && canShuffle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sortear papéis")
            }
        }
    }
}

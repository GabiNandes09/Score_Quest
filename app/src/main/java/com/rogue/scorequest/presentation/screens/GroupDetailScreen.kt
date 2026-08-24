package com.rogue.scorequest.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.GroupStats
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.presentation.components.BarChartEntry
import com.rogue.scorequest.presentation.components.HorizontalBarChart
import com.rogue.scorequest.presentation.components.PlayerAvatarImage
import com.rogue.scorequest.presentation.components.StatIconItem
import com.rogue.scorequest.presentation.viewmodel.GroupDetailViewModel
import com.rogue.scorequest.ui.theme.Gold
import com.rogue.scorequest.utils.formatDuration
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: GroupDetailViewModel = koinViewModel(parameters = { parametersOf(groupId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val group = state.group ?: return
    val stats = state.stats ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(groupId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar grupo")
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
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PlayerAvatarImage(
                    avatarPath = group.photoPath,
                    nickname = group.name,
                    modifier = Modifier.size(96.dp)
                )
            }

            MembersRow(members = state.members)
            CoreStatsCard(stats)
            MemberWinsCard(stats)
            stats.favoriteGame?.let { FavoriteGameCard(gameName = it.gameName, playCount = it.playCount) }
            TopGamesCard(stats)
        }
    }
}

@Composable
private fun MembersRow(members: List<Player>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(members) { player ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PlayerAvatarImage(
                    avatarPath = player.avatarPath,
                    nickname = player.nickname,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Text(text = player.nickname, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun CoreStatsCard(stats: GroupStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatIconItem(icon = Icons.Filled.PlayArrow, value = "${stats.gamesPlayed}x")
            StatIconItem(icon = Icons.Filled.Timer, value = formatDuration(stats.totalMinutes))
        }
    }
}

@Composable
private fun MemberWinsCard(stats: GroupStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Mais vitórias no grupo", style = MaterialTheme.typography.titleMedium, color = Gold)
            if (stats.memberWins.isEmpty()) {
                Text(
                    text = "Nenhuma partida registrada ainda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                HorizontalBarChart(
                    entries = stats.memberWins.mapIndexed { index, player ->
                        BarChartEntry(
                            label = player.playerName,
                            value = player.wins.toFloat(),
                            displayValue = "${player.wins}x",
                            highlighted = index == 0
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoriteGameCard(gameName: String, playCount: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Jogo favorito", style = MaterialTheme.typography.titleMedium, color = Gold)
            Text(text = gameName, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "$playCount ${if (playCount == 1) "partida" else "partidas"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TopGamesCard(stats: GroupStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Jogos mais jogados", style = MaterialTheme.typography.titleMedium, color = Gold)
            if (stats.topGames.isEmpty()) {
                Text(
                    text = "Nenhuma partida registrada ainda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                HorizontalBarChart(
                    entries = stats.topGames.mapIndexed { index, game ->
                        BarChartEntry(
                            label = game.gameName,
                            value = game.playCount.toFloat(),
                            displayValue = "${game.playCount}x",
                            highlighted = index == 0
                        )
                    }
                )
            }
        }
    }
}

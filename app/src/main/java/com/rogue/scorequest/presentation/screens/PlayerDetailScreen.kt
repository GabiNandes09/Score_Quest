package com.rogue.scorequest.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Percent
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
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.PlayerStats
import com.rogue.scorequest.presentation.components.BarChartEntry
import com.rogue.scorequest.presentation.components.HorizontalBarChart
import com.rogue.scorequest.presentation.components.PlayerAvatarImage
import com.rogue.scorequest.presentation.components.StatIconItem
import com.rogue.scorequest.presentation.viewmodel.PlayerDetailViewModel
import com.rogue.scorequest.ui.theme.Gold
import com.rogue.scorequest.utils.formatDuration
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    playerId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: PlayerDetailViewModel = koinViewModel(parameters = { parametersOf(playerId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player = state.player ?: return
    val stats = state.stats ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(player.nickname) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(playerId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar jogador")
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
                    avatarPath = player.avatarPath,
                    nickname = player.nickname,
                    modifier = Modifier.size(96.dp)
                )
            }

            CoreStatsCard(stats)
            StreakCard(stats)
            stats.favoriteGame?.let { FavoriteGameCard(gameName = it.gameName, wins = it.playCount) }
            TopGamesCard(stats)
        }
    }
}

@Composable
private fun CoreStatsCard(stats: PlayerStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatIconItem(icon = Icons.Filled.PlayArrow, value = "${stats.gamesPlayed}x")
            StatIconItem(icon = Icons.Filled.EmojiEvents, value = "${stats.wins}x")
            StatIconItem(
                icon = Icons.Filled.Percent,
                value = stats.winRate?.let { "${(it * 100).roundToInt()}%" } ?: "—"
            )
            StatIconItem(icon = Icons.Filled.Timer, value = formatDuration(stats.totalMinutes))
        }
    }
}

@Composable
private fun StreakCard(stats: PlayerStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Sequência de vitórias", style = MaterialTheme.typography.titleMedium, color = Gold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatIconItem(icon = Icons.Filled.LocalFireDepartment, value = "${stats.currentWinStreak} atual")
                StatIconItem(icon = Icons.AutoMirrored.Filled.TrendingUp, value = "${stats.bestWinStreak} recorde")
            }
        }
    }
}

@Composable
private fun FavoriteGameCard(gameName: String, wins: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Jogo favorito", style = MaterialTheme.typography.titleMedium, color = Gold)
            Text(text = gameName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = "$wins ${if (wins == 1) "vitória" else "vitórias"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TopGamesCard(stats: PlayerStats) {
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

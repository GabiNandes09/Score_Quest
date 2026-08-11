package com.rogue.scorequest.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.GamePlayCount
import com.rogue.scorequest.domain.model.MonthlyPlaytime
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.SessionWithDetails
import com.rogue.scorequest.presentation.components.BarChartEntry
import com.rogue.scorequest.presentation.components.GameCoverImage
import com.rogue.scorequest.presentation.components.HorizontalBarChart
import com.rogue.scorequest.presentation.components.LineChart
import com.rogue.scorequest.presentation.components.LineChartEntry
import com.rogue.scorequest.presentation.components.StatIconItem
import com.rogue.scorequest.presentation.viewmodel.HomeViewModel
import com.rogue.scorequest.presentation.viewmodel.states.HomeState
import com.rogue.scorequest.ui.theme.Gold
import com.rogue.scorequest.utils.formatDuration
import com.rogue.scorequest.utils.toRelativeDayString
import com.rogue.scorequest.utils.toShortMonthLabel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onRegisterSessionClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onRegisterSessionClick = onRegisterSessionClick,
        onNotificationsClick = onNotificationsClick
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onRegisterSessionClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.displayName.isNotBlank()) "Olá, ${state.displayName}" else "Olá!",
                style = MaterialTheme.typography.headlineSmall
            )
            IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Filled.Notifications, contentDescription = null)
            }
        }

        RegisterSessionButton(onClick = onRegisterSessionClick)

        HomeStatsCard(
            totalSessions = state.totalSessions,
            totalMinutes = state.totalMinutes,
            streakDays = state.streakDays,
            isStreakActive = state.isStreakActive
        )

        state.lastSession?.let { session ->
            LastSessionCard(session = session, players = state.players)
        }

        if (state.topGames.isNotEmpty()) {
            TopGamesChartCard(state.topGames)
        }

        if (state.monthlyPlaytime.isNotEmpty()) {
            PlaytimeChartCard(state.monthlyPlaytime)
        }
    }
}

@Composable
private fun RegisterSessionButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Gold, Color.White)))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Casino, contentDescription = null, tint = Color.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Registrar partida", color = Color.Black, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HomeStatsCard(
    totalSessions: Int,
    totalMinutes: Int,
    streakDays: Int,
    isStreakActive: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatIconItem(icon = Icons.Filled.PlayArrow, value = "${totalSessions}x")
            StatIconItem(icon = Icons.Filled.Timer, value = formatDuration(totalMinutes))
            StatIconItem(
                icon = if (isStreakActive) Icons.Filled.LocalFireDepartment else Icons.Filled.AcUnit,
                value = "$streakDays ${if (streakDays == 1) "dia" else "dias"}"
            )
        }
    }
}

@Composable
private fun LastSessionCard(session: SessionWithDetails, players: List<Player>) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val borderShape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(Gold, Color.White)),
                shape = borderShape
            ),
        shape = borderShape
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) { page ->
            if (page == 0) {
                LastSessionSummaryPage(session, players)
            } else {
                LastSessionParticipantsPage(session, players)
            }
        }
    }
}

@Composable
private fun LastSessionSummaryPage(session: SessionWithDetails, players: List<Player>) {
    Row(modifier = Modifier.fillMaxSize()) {
        GameCoverImage(
            coverImageUrl = session.session.photoUri ?: session.gameCoverImageUrl,
            gameName = session.gameName,
            modifier = Modifier
                .fillMaxHeight()
                .width(120.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Última jogatina",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = session.gameName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            val winnerName = session.scores
                .find { it.isWinner == true }
                ?.let { score -> players.find { it.id == score.playerId }?.nickname }
                ?: "—"
            Text(text = "Ganhador: $winnerName", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = session.session.date.toRelativeDayString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TopGamesChartCard(topGames: List<GamePlayCount>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Top jogos", style = MaterialTheme.typography.titleMedium)
            HorizontalBarChart(
                entries = topGames.map { game ->
                    BarChartEntry(
                        label = game.gameName,
                        value = game.playCount.toFloat(),
                        displayValue = "${game.playCount}x"
                    )
                }
            )
        }
    }
}

@Composable
private fun PlaytimeChartCard(monthlyPlaytime: List<MonthlyPlaytime>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Tempo de jogo (últimos 12 meses)", style = MaterialTheme.typography.titleMedium)
            LineChart(
                entries = monthlyPlaytime.map { month ->
                    LineChartEntry(
                        label = month.yearMonth.toShortMonthLabel(),
                        value = month.totalMinutes.toFloat()
                    )
                }
            )
        }
    }
}

@Composable
private fun LastSessionParticipantsPage(session: SessionWithDetails, players: List<Player>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        session.scores.forEach { score ->
            val nickname = players.find { it.id == score.playerId }?.nickname ?: score.playerId
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = nickname, style = MaterialTheme.typography.bodyMedium)
                    if (score.isWinner == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = Gold
                        )
                    }
                }
                Text(text = "${score.totalScore ?: "-"}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

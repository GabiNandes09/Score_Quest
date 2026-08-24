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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.ActiveTimer
import com.rogue.scorequest.domain.model.DayActivity
import com.rogue.scorequest.domain.model.DurationBucket
import com.rogue.scorequest.domain.model.GamePlayCount
import com.rogue.scorequest.domain.model.MonthSessionCount
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.SessionWithDetails
import com.rogue.scorequest.domain.model.TimerStatus
import com.rogue.scorequest.domain.model.hasNumberedPositionsForDisplay
import com.rogue.scorequest.domain.model.orderedForDisplay
import com.rogue.scorequest.presentation.components.ActivityHeatmap
import com.rogue.scorequest.presentation.components.BarChartEntry
import com.rogue.scorequest.presentation.components.GameCoverImage
import com.rogue.scorequest.presentation.components.HorizontalBarChart
import com.rogue.scorequest.presentation.components.LineChart
import com.rogue.scorequest.presentation.components.LineChartEntry
import com.rogue.scorequest.presentation.components.PlayerIdentityRow
import com.rogue.scorequest.presentation.components.PositionBadge
import com.rogue.scorequest.presentation.components.StatIconItem
import com.rogue.scorequest.presentation.components.VerticalBarChart
import com.rogue.scorequest.presentation.components.VerticalBarEntry
import com.rogue.scorequest.presentation.viewmodel.HomeViewModel
import com.rogue.scorequest.presentation.viewmodel.states.HomeState
import com.rogue.scorequest.ui.theme.Gold
import com.rogue.scorequest.utils.formatDuration
import com.rogue.scorequest.utils.formatElapsed
import com.rogue.scorequest.utils.toRelativeDayString
import com.rogue.scorequest.utils.toShortMonthLabel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

private const val MIN_SESSIONS_FOR_HISTOGRAM = 5
private const val MIN_MONTHS_FOR_TIMELINE = 3

@Composable
fun HomeScreen(
    onRegisterSessionClick: () -> Unit = {},
    onStartLiveMatchClick: () -> Unit = {},
    onResumeLiveMatchClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onGameClick: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onRegisterSessionClick = onRegisterSessionClick,
        onStartLiveMatchClick = onStartLiveMatchClick,
        onResumeLiveMatchClick = onResumeLiveMatchClick,
        onNotificationsClick = onNotificationsClick,
        onGameClick = onGameClick
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onRegisterSessionClick: () -> Unit,
    onStartLiveMatchClick: () -> Unit,
    onResumeLiveMatchClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onGameClick: (String) -> Unit
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

        state.activeTimer?.let { timer ->
            ActiveTimerBanner(timer = timer, onClick = { onResumeLiveMatchClick(timer.gameId) })
        }

        RegisterSessionButton(onClick = onRegisterSessionClick)

        if (state.activeTimer == null) {
            OutlinedButton(onClick = onStartLiveMatchClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Timer, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar partida ao vivo")
            }
        }

        HomeStatsCard(
            totalSessions = state.totalSessions,
            weekMinutes = state.weekMinutes,
            totalMinutes = state.totalMinutes,
            streakDays = state.streakDays,
            isStreakActive = state.isStreakActive
        )

        state.lastSession?.let { session ->
            LastSessionCard(session = session, players = state.players)
        }

        ActivityHeatmapCard(state.activityHeatmap)

        RankingCard(topGames = state.topGames, onGameClick = onGameClick)

        TimelineCard(state.sessionsByMonth)

        if (state.totalSessions >= MIN_SESSIONS_FOR_HISTOGRAM) {
            HistogramCard(state.durationHistogram)
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
private fun ActiveTimerBanner(timer: ActiveTimer, onClick: () -> Unit) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timer.status) {
        while (timer.status == TimerStatus.RUNNING) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (timer.status == TimerStatus.PAUSED) "Partida em pausa" else "Partida em andamento",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = timer.gameName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Text(
            text = formatElapsed(timer.elapsedMillis(nowMillis)),
            style = MaterialTheme.typography.titleLarge,
            color = Gold
        )
    }
}

@Composable
private fun HomeStatsCard(
    totalSessions: Int,
    weekMinutes: Int,
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
            StatIconItem(icon = Icons.Filled.DateRange, value = formatDuration(weekMinutes))
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
private fun LastSessionParticipantsPage(session: SessionWithDetails, players: List<Player>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val showPositions = session.scores.hasNumberedPositionsForDisplay()
        val orderedScores = session.scores.orderedForDisplay()
        orderedScores.forEachIndexed { index, score ->
            val nickname = players.find { it.id == score.playerId }?.nickname ?: score.playerId
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showPositions) {
                        PositionBadge(position = index + 1, modifier = Modifier.padding(end = 8.dp))
                    }
                    PlayerIdentityRow(name = nickname, isWinner = score.isWinner == true)
                }
                if (showPositions) {
                    if (score.totalScore != null) Text(text = "${score.totalScore}", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(text = "${score.totalScore ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ActivityHeatmapCard(days: List<DayActivity>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Atividade recente", style = MaterialTheme.typography.titleMedium)
            ActivityHeatmap(days = days, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RankingCard(topGames: List<GamePlayCount>, onGameClick: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Ranking dos mais jogados", style = MaterialTheme.typography.titleMedium)
            if (topGames.isEmpty()) {
                Text(
                    text = "Jogue sua primeira partida pra ver seu ranking aqui",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                HorizontalBarChart(
                    entries = topGames.mapIndexed { index, game ->
                        BarChartEntry(
                            label = game.gameName,
                            value = game.playCount.toFloat(),
                            displayValue = "${game.playCount}x",
                            highlighted = index == 0,
                            onClick = { onGameClick(game.gameId) }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TimelineCard(sessionsByMonth: List<MonthSessionCount>) {
    val nonZeroMonths = sessionsByMonth.count { it.sessionCount > 0 }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Partidas por mês", style = MaterialTheme.typography.titleMedium)
            if (nonZeroMonths < MIN_MONTHS_FOR_TIMELINE) {
                val firstMonthLabel = sessionsByMonth.firstOrNull { it.sessionCount > 0 }?.month?.toShortMonthLabel()
                Text(
                    text = firstMonthLabel?.let { "Você começou a registrar partidas em $it" }
                        ?: "Nenhuma partida registrada este ano ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LineChart(
                    entries = sessionsByMonth.map { month ->
                        LineChartEntry(label = month.month.toShortMonthLabel(), value = month.sessionCount.toFloat())
                    },
                    showAreaFill = true
                )
            }
        }
    }
}

@Composable
private fun HistogramCard(histogram: List<DurationBucket>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Duração das partidas", style = MaterialTheme.typography.titleMedium)
            VerticalBarChart(
                entries = histogram.map { bucket ->
                    VerticalBarEntry(
                        label = bucket.label,
                        value = bucket.sessionCount.toFloat(),
                        displayValue = "${bucket.sessionCount}"
                    )
                }
            )
        }
    }
}

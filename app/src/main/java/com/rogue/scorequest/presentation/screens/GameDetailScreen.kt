package com.rogue.scorequest.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.SessionWithDetails
import com.rogue.scorequest.presentation.components.GameCoverImage
import com.rogue.scorequest.presentation.components.StatIconItem
import com.rogue.scorequest.presentation.viewmodel.GameDetailViewModel
import com.rogue.scorequest.ui.theme.Gold
import com.rogue.scorequest.utils.formatDuration
import com.rogue.scorequest.utils.toRelativeDayString
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GameDetailScreen(
    gameId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onRegisterSessionClick: () -> Unit,
    onStartLiveMatchClick: () -> Unit,
    onSessionClick: (String) -> Unit,
    onConfigureScoreClick: () -> Unit,
    viewModel: GameDetailViewModel = koinViewModel(parameters = { parametersOf(gameId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val game = state.game ?: return

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                GameCoverImage(
                    coverImageUrl = game.game.coverImageUrl,
                    gameName = game.game.name,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent)
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .align(Alignment.TopCenter)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = game.game.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                RatingRow(
                    rating = game.libraryEntry?.rating ?: 0,
                    onRatingChange = viewModel::onRatingChange
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatIconItem(
                    icon = Icons.Filled.Groups,
                    value = "${game.game.minPlayers}-${game.game.maxPlayers}"
                )
                StatIconItem(
                    icon = Icons.Filled.Timer,
                    value = "${game.game.avgDurationMinutes} min"
                )
                StatIconItem(
                    icon = Icons.Filled.FitnessCenter,
                    value = game.game.weight?.toString() ?: "—"
                )
            }
        }

        item {
            StatusDropdown(
                selected = game.libraryEntry?.status,
                onSelected = viewModel::onStatusChange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (game.libraryEntry?.status == LibraryStatus.HAVE) {
            item {
                LoanSection(
                    lentTo = game.libraryEntry.lentTo,
                    onLoanChange = viewModel::onLoanChange,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        state.stats?.let { stats ->
            item {
                val totalMinutes = state.sessions.sumOf { it.session.durationMinutes }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Estatísticas", style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatIconItem(
                                icon = Icons.Filled.PlayArrow,
                                value = "${stats.timesPlayed}x"
                            )
                            StatIconItem(
                                icon = Icons.Filled.Timer,
                                value = formatDuration(totalMinutes)
                            )
                            StatIconItem(
                                icon = Icons.Filled.AccessTime,
                                value = formatDuration(stats.avgDurationMinutes)
                            )
                        }
                    }
                }
            }
        }

        if (state.activeTimer == null) {
            item {
                OutlinedButton(
                    onClick = onStartLiveMatchClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Filled.Timer, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar partida ao vivo")
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onConfigureScoreClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(if (state.hasScoreSchema) "Editar pontuação personalizada" else "Configurar pontuação personalizada")
            }
        }

        if (state.sessions.isNotEmpty()) {
            item {
                Text(
                    text = "Histórico de partidas",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(state.sessions) { session ->
                SessionHistoryRow(
                    session = session,
                    onClick = { onSessionClick(session.session.id) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        item { Box(modifier = Modifier.padding(bottom = 96.dp)) }
    }

        FloatingActionButton(
            onClick = onRegisterSessionClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}

@Composable
private fun StatusDropdown(
    selected: LibraryStatus?,
    onSelected: (LibraryStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(Gold, Color.White)))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected?.let { statusLabel(it) } ?: "Selecionar status",
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.Black)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LibraryStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(statusLabel(status)) },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LoanSection(
    lentTo: String?,
    onLoanChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLent by remember(lentTo) { mutableStateOf(lentTo != null) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isLent,
                onCheckedChange = { checked ->
                    isLent = checked
                    if (!checked) onLoanChange(null)
                }
            )
            Text("Emprestado")
        }

        if (isLent) {
            OutlinedTextField(
                value = lentTo.orEmpty(),
                onValueChange = { onLoanChange(it.ifBlank { null }) },
                label = { Text("Emprestado para") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun RatingRow(rating: Int, onRatingChange: (Int) -> Unit) {
    Row {
        for (star in 1..5) {
            IconButton(onClick = { onRatingChange(star) }) {
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(
    session: SessionWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = session.session.date.toRelativeDayString())
            Text(
                text = "${session.session.participantIds.size} jogadores · ${session.session.durationMinutes} min",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun statusLabel(status: LibraryStatus): String = when (status) {
    LibraryStatus.HAVE -> "Tenho"
    LibraryStatus.WANT -> "Quero"
    LibraryStatus.DONT_HAVE -> "Não tenho"
}

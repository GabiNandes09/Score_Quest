package com.rogue.scorequest.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.rogue.scorequest.domain.model.BoardGame
import com.rogue.scorequest.domain.model.SessionWithDetails
import com.rogue.scorequest.presentation.viewmodel.ProfileViewModel
import com.rogue.scorequest.presentation.viewmodel.states.ProfileTab
import com.rogue.scorequest.utils.toRelativeDayString
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSessionClick: (String) -> Unit,
    onEditProfileClick: () -> Unit,
    onEditFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagedSessions = viewModel.pagedSessions.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.displayName.isNotBlank()) state.displayName else "Perfil") },
                actions = {
                    TextButton(onClick = onEditProfileClick) { Text("Editar") }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Configurações")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "${state.sessionCount} partidas",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            val tabIndex = if (state.selectedTab == ProfileTab.FAVORITES) 0 else 1
            SecondaryTabRow(selectedTabIndex = tabIndex) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { viewModel.onTabSelected(ProfileTab.FAVORITES) },
                    text = { Text("Favoritos") }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { viewModel.onTabSelected(ProfileTab.ACTIVITIES) },
                    text = { Text("Atividades") }
                )
            }

            when (state.selectedTab) {
                ProfileTab.FAVORITES -> FavoritesTab(state.favoriteGames, onEditFavoritesClick)
                ProfileTab.ACTIVITIES -> ActivitiesTab(pagedSessions, onSessionClick)
            }
        }
    }
}

@Composable
private fun FavoritesTab(
    favorites: List<BoardGame>,
    onEditFavoritesClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onEditFavoritesClick) {
            Text("Editar favoritos")
        }
        if (favorites.isEmpty()) {
            Text("Nenhum jogo favorito ainda")
        }
        favorites.forEach { game ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(text = game.name, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ActivitiesTab(
    pagedSessions: androidx.paging.compose.LazyPagingItems<SessionWithDetails>,
    onSessionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pagedSessions.itemCount) { index ->
            val session = pagedSessions[index]
            if (session != null) {
                ActivityRow(session, onClick = { onSessionClick(session.session.id) })
            }
        }
    }
}

@Composable
private fun ActivityRow(session: SessionWithDetails, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = session.gameName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${session.session.participantIds.size} jogadores · ${session.session.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(text = session.session.date.toRelativeDayString(), style = MaterialTheme.typography.bodySmall)
        }
    }
}

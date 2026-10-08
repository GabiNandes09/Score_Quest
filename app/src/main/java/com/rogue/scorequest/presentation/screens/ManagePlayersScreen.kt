package com.rogue.scorequest.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.data.cloud.CloudFriend
import com.rogue.scorequest.data.cloud.CloudFriendRequest
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.PlayerGroup
import com.rogue.scorequest.presentation.components.PlayerAvatarImage
import com.rogue.scorequest.presentation.viewmodel.FriendsViewModel
import com.rogue.scorequest.presentation.viewmodel.ManagePlayersViewModel
import com.rogue.scorequest.presentation.viewmodel.states.FriendsState
import com.rogue.scorequest.ui.theme.Gold
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePlayersScreen(
    onPlayerClick: (String) -> Unit,
    onAddPlayerClick: () -> Unit,
    onGroupClick: (String) -> Unit,
    onAddGroupClick: () -> Unit,
    viewModel: ManagePlayersViewModel = koinViewModel(),
    friendsViewModel: FriendsViewModel = koinViewModel()
) {
    val players by viewModel.players.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val friendsState by friendsViewModel.state.collectAsStateWithLifecycle()
    var searchExpanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    val filteredPlayers = remember(players, query) {
        players.filter { query.isBlank() || it.nickname.contains(query, ignoreCase = true) }
    }
    val filteredGroups = remember(groups, query) {
        groups.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jogadores") },
                actions = {
                    if (selectedTab != 2) {
                        IconButton(onClick = { searchExpanded = !searchExpanded }) {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                        TextButton(onClick = if (selectedTab == 0) onAddPlayerClick else onAddGroupClick) {
                            Text(if (selectedTab == 0) "Adicionar Jogador" else "Adicionar Grupo")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AnimatedVisibility(
                visible = searchExpanded && selectedTab != 2,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(220)),
                exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(180))
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar por nome") },
                    singleLine = true
                )
            }

            SecondaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Jogadores", color = tabTextColor(selectedTab == 0)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Grupos", color = tabTextColor(selectedTab == 1)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Amigos", color = tabTextColor(selectedTab == 2)) }
                )
            }

            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 })
                        .togetherWith(fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { -it / 8 })
                },
                label = "manage_players_tab_content"
            ) { tab ->
                if (tab == 2) {
                    FriendsTab(state = friendsState, viewModel = friendsViewModel)
                } else if (tab == 0) {
                    if (players.isNotEmpty() && filteredPlayers.isEmpty()) {
                        Text(
                            text = "Nenhum jogador encontrado",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredPlayers) { player ->
                                PlayerGridItem(player = player, onClick = { onPlayerClick(player.id) })
                            }
                        }
                    }
                } else {
                    if (groups.isNotEmpty() && filteredGroups.isEmpty()) {
                        Text(
                            text = "Nenhum grupo encontrado",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (groups.isEmpty()) {
                        Text(
                            text = "Crie um grupo pra selecionar vários jogadores de uma vez ao registrar partidas",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredGroups) { group ->
                                GroupGridItem(group = group, onClick = { onGroupClick(group.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun tabTextColor(selected: Boolean) =
    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun PlayerGridItem(
    player: Player,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            PlayerAvatarImage(
                avatarPath = player.avatarPath,
                nickname = player.nickname,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = player.nickname,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun GroupGridItem(
    group: PlayerGroup,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            PlayerAvatarImage(
                avatarPath = group.photoPath,
                nickname = group.name,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = group.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FriendsTab(
    state: FriendsState,
    viewModel: FriendsViewModel
) {
    if (!state.isLoggedIn) {
        Text(
            text = "Entre com sua conta Google em Configurações pra adicionar amigos.",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("@username") },
                    singleLine = true
                )
                Button(onClick = viewModel::onSearchClick, enabled = !state.isSearching) {
                    Text("Buscar")
                }
            }
        }

        state.searchError?.let { error ->
            item { Text(text = error, color = MaterialTheme.colorScheme.error) }
        }

        state.searchResult?.let { result ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlayerAvatarImage(
                            avatarPath = result.photoUrl,
                            nickname = result.displayName ?: result.username,
                            modifier = Modifier.size(48.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = result.displayName ?: result.username, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "@${result.username}", style = MaterialTheme.typography.bodySmall, color = Gold)
                        }
                        OutlinedButton(onClick = viewModel::onSendRequestClick) {
                            Text("Adicionar")
                        }
                    }
                }
            }
        }

        if (state.incomingRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Solicitações recebidas",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.incomingRequests) { request ->
                FriendRequestRow(
                    request = request,
                    onAccept = { viewModel.onAcceptClick(request) },
                    onDecline = { viewModel.onDeclineClick(request) }
                )
            }
        }

        item {
            Text(
                text = "Meus amigos",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (state.friends.isEmpty()) {
            item { Text("Você ainda não tem amigos adicionados") }
        } else {
            items(state.friends) { friend ->
                FriendRow(
                    friend = friend,
                    isLinkedToPlayer = friend.uid in state.linkedFriendUids,
                    onAddAsPlayer = { viewModel.onAddAsPlayerClick(friend) },
                    onRemove = { viewModel.onRemoveFriendClick(friend) }
                )
            }
        }
    }

    state.sendRequestMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissSendRequestMessage,
            title = { Text("Amigos") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissSendRequestMessage) { Text("OK") }
            }
        )
    }
}

@Composable
private fun FriendRequestRow(
    request: CloudFriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayerAvatarImage(
                avatarPath = request.fromPhotoUrl,
                nickname = request.fromDisplayName ?: request.fromUsername,
                modifier = Modifier.size(48.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = request.fromDisplayName ?: request.fromUsername, style = MaterialTheme.typography.bodyLarge)
                Text(text = "@${request.fromUsername}", style = MaterialTheme.typography.bodySmall, color = Gold)
            }
            TextButton(onClick = onDecline) { Text("Recusar") }
            Spacer(modifier = Modifier.width(4.dp))
            Button(onClick = onAccept) { Text("Aceitar") }
        }
    }
}

@Composable
private fun FriendRow(
    friend: CloudFriend,
    isLinkedToPlayer: Boolean,
    onAddAsPlayer: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayerAvatarImage(
                avatarPath = friend.photoUrl,
                nickname = friend.displayName ?: friend.username,
                modifier = Modifier.size(48.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = friend.displayName ?: friend.username, style = MaterialTheme.typography.bodyLarge)
                Text(text = "@${friend.username}", style = MaterialTheme.typography.bodySmall, color = Gold)
            }
            if (isLinkedToPlayer) {
                Text(
                    text = "Já é jogador",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                OutlinedButton(onClick = onAddAsPlayer) {
                    Text("Adicionar como jogador")
                }
            }
            TextButton(onClick = onRemove) {
                Text("Remover")
            }
        }
    }
}

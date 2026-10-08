package com.rogue.scorequest.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.presentation.components.ImagePickerSection
import com.rogue.scorequest.presentation.components.PlayerAvatarImage
import com.rogue.scorequest.presentation.viewmodel.AddEditPlayerViewModel
import com.rogue.scorequest.ui.theme.Gold
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlayerScreen(
    playerId: String,
    onBackClick: () -> Unit,
    viewModel: AddEditPlayerViewModel = koinViewModel(parameters = { parametersOf(playerId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) onBackClick()
    }
    LaunchedEffect(state.deleted) {
        if (state.deleted) onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Editar jogador" else "Adicionar jogador") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ImagePickerSection(
                currentPath = state.avatarPath,
                onCaptured = viewModel::onAvatarCaptured
            )

            OutlinedTextField(
                value = state.nickname,
                onValueChange = viewModel::onNicknameChange,
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = viewModel::save,
                enabled = state.isValid && !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }

            if (state.isEditMode && state.isLoggedIn) {
                if (state.linkedUserId != null) {
                    Text(
                        text = "Vinculado a @${state.linkedUsername ?: "..."}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gold
                    )
                } else {
                    OutlinedButton(
                        onClick = viewModel::onOpenLinkFriendDialog,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Vincular a um amigo")
                    }
                }
            }

            if (state.isEditMode) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Excluir jogador")
                }
            }
        }
    }

    if (state.showLinkFriendDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissLinkFriendDialog,
            title = { Text("Vincular a um amigo") },
            text = {
                if (state.availableFriendsToLink.isEmpty()) {
                    Text("Nenhum amigo disponível pra vincular (todos já estão vinculados a outro jogador, ou você ainda não tem amigos).")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(state.availableFriendsToLink) { friend ->
                            Card(
                                onClick = { viewModel.onLinkToFriend(friend) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    PlayerAvatarImage(
                                        avatarPath = friend.photoUrl,
                                        nickname = friend.displayName ?: friend.username,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Column {
                                        Text(text = friend.displayName ?: friend.username)
                                        Text(text = "@${friend.username}", style = MaterialTheme.typography.bodySmall, color = Gold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissLinkFriendDialog) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir jogador") },
            text = { Text("Tem certeza que deseja excluir este jogador?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    state.deleteError?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteError,
            title = { Text("Não foi possível excluir") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissDeleteError) { Text("Ok") }
            }
        )
    }
}

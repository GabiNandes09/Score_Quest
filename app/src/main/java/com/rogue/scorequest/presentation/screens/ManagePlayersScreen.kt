package com.rogue.scorequest.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.presentation.viewmodel.ManagePlayersViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePlayersScreen(
    onBackClick: () -> Unit,
    viewModel: ManagePlayersViewModel = koinViewModel()
) {
    val players by viewModel.players.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var playerBeingRenamed by remember { mutableStateOf<Player?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar jogadores") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(players) { player ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = player.nickname, style = MaterialTheme.typography.bodyLarge)
                    Row {
                        IconButton(onClick = { playerBeingRenamed = player }) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                        }
                        IconButton(onClick = { viewModel.delete(player.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    playerBeingRenamed?.let { player ->
        var newNickname by remember(player.id) { mutableStateOf(player.nickname) }
        AlertDialog(
            onDismissRequest = { playerBeingRenamed = null },
            title = { Text("Editar apelido") },
            text = {
                OutlinedTextField(
                    value = newNickname,
                    onValueChange = { newNickname = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rename(player, newNickname)
                    playerBeingRenamed = null
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { playerBeingRenamed = null }) { Text("Cancelar") }
            }
        )
    }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Não foi possível excluir") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("Ok") }
            }
        )
    }
}

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.presentation.viewmodel.EditFavoritesViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFavoritesScreen(
    onBackClick: () -> Unit,
    viewModel: EditFavoritesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pendingReplacementGameId by viewModel.pendingReplacementGameId.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar favoritos") },
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(state.allGames) { game ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = game.id in state.favoriteIds,
                        onCheckedChange = { viewModel.toggleFavorite(game.id) }
                    )
                    Text(text = game.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    if (pendingReplacementGameId != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelReplace,
            title = { Text("Limite de favoritos") },
            text = { Text("Você já tem 3 jogos favoritos. Substituir o mais antigo por este?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmReplace) { Text("Substituir") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelReplace) { Text("Cancelar") }
            }
        )
    }
}

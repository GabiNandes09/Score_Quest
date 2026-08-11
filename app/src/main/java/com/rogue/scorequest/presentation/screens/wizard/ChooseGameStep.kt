package com.rogue.scorequest.presentation.screens.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseGameStep(
    viewModel: AddSessionViewModel,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (viewModel.consumeAutoAdvance()) {
            onNext()
        }
    }

    val sortedFiltered = remember(games, query) {
        games
            .filter { query.isBlank() || it.game.name.contains(query, ignoreCase = true) }
            .sortedByDescending { it.libraryEntry?.status == LibraryStatus.HAVE }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escolher jogo") },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancelar") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar jogo") },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedFiltered) { item ->
                    GameOptionRow(item) {
                        viewModel.onGameSelected(item.game.id, item.game.name)
                        onNext()
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOptionRow(item: GameWithLibraryInfo, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.game.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${item.game.minPlayers}-${item.game.maxPlayers} jogadores",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

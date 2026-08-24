package com.rogue.scorequest.presentation.screens.tools

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.ui.theme.Gold
import java.util.UUID

private data class ScratchPlayer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val score: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchScoreboardScreen(onBackClick: () -> Unit) {
    var players by remember { mutableStateOf(listOf<ScratchPlayer>()) }
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Placar avulso") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nome do jogador") },
                    singleLine = true
                )
                OutlinedButton(onClick = {
                    if (newName.isNotBlank()) {
                        players = players + ScratchPlayer(name = newName.trim())
                        newName = ""
                    }
                }) {
                    Text("+ Adicionar")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players, key = { it.id }) { player ->
                    ScratchPlayerRow(
                        player = player,
                        onIncrement = { players = players.map { if (it.id == player.id) it.copy(score = it.score + 1) else it } },
                        onDecrement = { players = players.map { if (it.id == player.id) it.copy(score = it.score - 1) else it } },
                        onRemove = { players = players.filter { it.id != player.id } }
                    )
                }
            }

            if (players.isNotEmpty()) {
                OutlinedButton(
                    onClick = { players = players.map { it.copy(score = 0) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zerar placar")
                }
            }
        }
    }
}

@Composable
private fun ScratchPlayerRow(
    player: ScratchPlayer,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDecrement) {
                Icon(Icons.Filled.Remove, contentDescription = null)
            }
            Text(
                text = "${player.score}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onIncrement) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = null)
            }
        }
    }
}

package com.rogue.scorequest.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.domain.model.Player

/**
 * Busca por nome + lista de checkboxes de jogadores — extraído de `AddEditGroupScreen` pra
 * ser reaproveitado em qualquer tela que precise selecionar vários jogadores (grupos,
 * ferramentas de sorteio).
 */
@Composable
fun PlayerMultiSelectSection(
    players: List<Player>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    listHeight: Dp = 240.dp
) {
    var query by remember { mutableStateOf("") }
    val filteredPlayers = remember(players, query) {
        players.filter { query.isBlank() || it.nickname.contains(query, ignoreCase = true) }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por nome") },
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(listHeight),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(filteredPlayers) { player ->
                PlayerRow(
                    player = player,
                    selected = player.id in selectedIds,
                    onToggle = { onToggle(player.id) }
                )
            }
        }
    }
}

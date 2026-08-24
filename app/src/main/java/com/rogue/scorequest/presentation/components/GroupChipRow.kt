package com.rogue.scorequest.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.domain.model.PlayerGroup

/**
 * Linha de chips de grupo (avatar + nome), usada como atalho de seleção rápida — extraído do
 * wizard de partida (`PlayersStep`) pra ser reaproveitado nas ferramentas de sorteio.
 */
@Composable
fun GroupChipRow(
    groups: List<PlayerGroup>,
    selectedGroupId: String?,
    onGroupClick: (PlayerGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groups) { group ->
            GroupChip(
                group = group,
                selected = group.id == selectedGroupId,
                onClick = { onGroupClick(group) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupChip(group: PlayerGroup, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = {
            PlayerAvatarImage(
                avatarPath = group.photoPath,
                nickname = group.name,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
            )
        },
        label = { Text(group.name) }
    )
}

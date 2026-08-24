package com.rogue.scorequest.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.rogue.scorequest.ui.theme.Gold

/** "1º"/"2º"/etc., dourado no 1º lugar — mesmo padrão usado em toda exibição de posição. */
@Composable
fun PositionBadge(position: Int?, modifier: Modifier = Modifier) {
    Text(
        text = position?.let { "${it}º" } ?: "-",
        fontWeight = FontWeight.Bold,
        color = if (position == 1) Gold else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

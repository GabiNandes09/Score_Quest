package com.rogue.scorequest.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.ui.theme.Gold

/**
 * Standard player-identity display: person icon on the left, name, trophy
 * marking the winner on the right of the name. Used everywhere a player row
 * is shown (last session card, session detail, wizard confirm/scoring).
 * Pass [onWinnerToggle] to make the trophy an interactive toggle (scoring
 * step); omit it for a read-only display that only shows the trophy when
 * [isWinner] is true.
 */
@Composable
fun PlayerIdentityRow(
    name: String,
    isWinner: Boolean,
    modifier: Modifier = Modifier,
    onWinnerToggle: (() -> Unit)? = null
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(4.dp))
        if (onWinnerToggle != null) {
            IconButton(onClick = onWinnerToggle) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = if (isWinner) Gold else MaterialTheme.colorScheme.outline
                )
            }
        } else if (isWinner) {
            Icon(imageVector = Icons.Filled.EmojiEvents, contentDescription = null, tint = Gold)
        }
    }
}

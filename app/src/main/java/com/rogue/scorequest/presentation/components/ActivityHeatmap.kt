package com.rogue.scorequest.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.domain.model.DayActivity
import com.rogue.scorequest.ui.theme.Gold
import com.rogue.scorequest.utils.toDayMonthLabel
import java.time.LocalDate

private val LEVEL_0 = Color(0xFF2C2C2C)
private val LEVEL_1 = Color(0xFF5C4A1A)
private val LEVEL_2 = Color(0xFF8A6E20)
private val LEVEL_3 = Color(0xFFB08F28)
private const val CELL_SIZE_DP = 12
private const val CELL_SPACING_DP = 2

private fun colorForCount(count: Int): Color = when {
    count <= 0 -> LEVEL_0
    count == 1 -> LEVEL_1
    count == 2 -> LEVEL_2
    count == 3 -> LEVEL_3
    else -> Gold
}

/**
 * GitHub-style contribution grid: one square per day, color intensity
 * proportional to session count that day. Expects [days] in chronological
 * order (oldest first); pads the front so the grid is a clean multiple of 7.
 */
@Composable
fun ActivityHeatmap(days: List<DayActivity>, modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<DayActivity?>(null) }
    val padding = (7 - days.size % 7) % 7
    val weeks = (List<DayActivity?>(padding) { null } + days).chunked(7)

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(CELL_SPACING_DP.dp)) {
            weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(CELL_SPACING_DP.dp)) {
                    week.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .width(CELL_SIZE_DP.dp)
                                .height(CELL_SIZE_DP.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(cell?.let { colorForCount(it.sessionCount) } ?: Color.Transparent)
                                .let { boxModifier ->
                                    if (cell != null) {
                                        boxModifier.clickable { selected = cell }
                                    } else {
                                        boxModifier
                                    }
                                }
                        )
                    }
                }
            }
        }
        val day = selected
        if (day != null) {
            val count = day.sessionCount
            Text(
                text = "${LocalDate.parse(day.day).toDayMonthLabel()} — $count ${if (count == 1) "partida" else "partidas"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

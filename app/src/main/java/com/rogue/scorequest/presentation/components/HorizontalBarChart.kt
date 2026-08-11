package com.rogue.scorequest.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class BarChartEntry(
    val label: String,
    val value: Float,
    val displayValue: String,
    val highlighted: Boolean = true,
    val onClick: (() -> Unit)? = null
)

/**
 * Single-hue magnitude bar chart (ranking/comparison of a few entries).
 * One row per entry: name on the left, a thin pill-shaped bar growing to the
 * right. Color encodes nothing here, so a single theme hue is used and
 * identity comes from the direct label.
 */
@Composable
fun HorizontalBarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier
) {
    val maxValue = entries.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f
    val barHeight = 14.dp

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        entries.forEach { entry ->
            val barColor = if (entry.highlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { rowModifier ->
                        entry.onClick?.let { onClick -> rowModifier.clickable(onClick = onClick) } ?: rowModifier
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(90.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(barHeight / 2))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (entry.value / maxValue).coerceIn(0.05f, 1f))
                            .clip(RoundedCornerShape(barHeight / 2))
                            .background(barColor)
                    )
                }
                Text(
                    text = entry.displayValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

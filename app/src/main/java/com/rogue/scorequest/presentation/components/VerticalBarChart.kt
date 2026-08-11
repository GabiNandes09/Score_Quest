package com.rogue.scorequest.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class VerticalBarEntry(
    val label: String,
    val value: Float,
    val displayValue: String
)

/**
 * Single-hue magnitude bar chart, vertical form (distribution across fixed
 * buckets). Bars are bottom-anchored, side by side, value above each bar and
 * bucket label below it.
 */
@Composable
fun VerticalBarChart(
    entries: List<VerticalBarEntry>,
    modifier: Modifier = Modifier,
    barsHeight: Dp = 120.dp
) {
    val maxValue = entries.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        entries.forEach { entry ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = entry.displayValue,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .height(barsHeight)
                        .width(40.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(fraction = (entry.value / maxValue).coerceIn(0.03f, 1f))
                            .width(28.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(56.dp)
                )
            }
        }
    }
}

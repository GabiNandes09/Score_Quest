package com.rogue.scorequest.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class LineChartEntry(
    val label: String,
    val value: Float
)

/**
 * Single-hue trend line (change over time, one series).
 * Thin 2dp poly-line with small rounded markers, recessive baseline grid line,
 * and a label row underneath every-Nth point to avoid crowding on narrow screens.
 */
@Composable
fun LineChart(
    entries: List<LineChartEntry>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 140.dp
) {
    if (entries.isEmpty()) return

    val maxValue = entries.maxOf { it.value }.takeIf { it > 0f } ?: 1f
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val labelStep = if (entries.size > 6) 2 else 1

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val stepX = size.width / entries.size
            val topPadding = 12.dp.toPx()
            val bottomPadding = 12.dp.toPx()
            val plotHeight = size.height - topPadding - bottomPadding

            drawLine(
                color = gridColor,
                start = Offset(0f, size.height - bottomPadding),
                end = Offset(size.width, size.height - bottomPadding),
                strokeWidth = 1.dp.toPx()
            )

            val points = entries.mapIndexed { index, entry ->
                val x = stepX * index + stepX / 2f
                val fraction = (entry.value / maxValue).coerceIn(0f, 1f)
                val y = topPadding + plotHeight * (1f - fraction)
                Offset(x, y)
            }

            val path = Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            points.forEach { point ->
                drawCircle(color = lineColor, radius = 3.dp.toPx(), center = point)
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            entries.forEachIndexed { index, entry ->
                Text(
                    text = if (index % labelStep == 0) entry.label else "",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

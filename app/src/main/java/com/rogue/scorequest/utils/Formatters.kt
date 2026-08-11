package com.rogue.scorequest.utils

fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> "${minutes}min"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}min"
    }
}

private val SHORT_MONTH_LABELS = arrayOf(
    "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
)

fun String.toShortMonthLabel(): String {
    val month = substringAfter('-').toIntOrNull() ?: return this
    return SHORT_MONTH_LABELS.getOrElse(month - 1) { this }
}

package com.rogue.scorequest.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

fun LocalDateTime.toRelativeDayString(): String {
    val days = ChronoUnit.DAYS.between(toLocalDate(), LocalDate.now())
    return when {
        days <= 0L -> "hoje"
        days == 1L -> "ontem"
        else -> "há $days dias"
    }
}

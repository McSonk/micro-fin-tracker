package com.eromn.microfintracker.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateUtils {
    private val mxLocal = Locale.Builder().setLanguage("es").setRegion("MX").build()
    fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", mxLocal)
        return localDateTime.format(formatter)
    }

    fun formatHeaderDate(timestamp: Long): String {
        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toLocalDate()
        val now = LocalDateTime.now(ZoneId.systemDefault()).toLocalDate()

        return when (date) {
            now -> "Hoy"
            now.minusDays(1) -> "Ayer"
            now.minusDays(2) -> "Ante ayer"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", mxLocal)
                date.format(formatter)
                date.format(formatter).replaceFirstChar { it.uppercase() }
            }
        }
    }
}
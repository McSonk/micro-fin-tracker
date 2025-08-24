package com.eromn.microfintracker.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateUtils {
    fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val mxLocal = Locale.Builder().setLanguage("es").setRegion("MX").build()

        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", mxLocal)
        return localDateTime.format(formatter)
    }
}
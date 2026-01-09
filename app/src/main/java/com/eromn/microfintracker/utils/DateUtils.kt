package com.eromn.microfintracker.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class DateUtils {
    private val mxLocal = Locale.Builder().setLanguage("es").setRegion("MX").build()
    fun formatTime(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("h:mm a", mxLocal)
        return localDateTime.format(formatter)
    }

    fun formatTime(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.isLenient = false

        return formatTime(cal.timeInMillis )
    }

    fun formatDate(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("d 'de' MMM", mxLocal)
        return localDateTime.format(formatter)
    }

    fun formatHeaderDate(timestamp: Long): String {
        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toLocalDate()
        val now = LocalDateTime.now(ZoneId.systemDefault()).toLocalDate()

        return when (date) {
            now -> "Hoy"
            now.minusDays(1) -> "Ayer, " + date.format(DateTimeFormatter.ofPattern("d 'de' MMM", mxLocal))
            now.minusDays(2) -> "Ante ayer, " + date.format(DateTimeFormatter.ofPattern("d 'de' MMM", mxLocal))
            else -> {
                val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", mxLocal)
                date.format(formatter).replaceFirstChar { it.uppercase() }
            }
        }
    }

    fun mergeDateTime(date: Long, hour: Int, minute: Int): Long {
        val targetDateTimeCalendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        targetDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hour)
        targetDateTimeCalendar.set(Calendar.MINUTE, minute)

        return targetDateTimeCalendar.timeInMillis
    }
}
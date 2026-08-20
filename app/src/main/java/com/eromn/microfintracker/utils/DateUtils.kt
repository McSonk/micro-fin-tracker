package com.eromn.microfintracker.utils

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class DateUtils {
    companion object {
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

        fun formatDateUTC(timestamp: Long): String {
            val instant = Instant.ofEpochMilli(timestamp)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))

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

        fun timeBetween(date1: Long, date2: Long): String {
            val instant1 = Instant.ofEpochMilli(date1)
            val instant2 = Instant.ofEpochMilli(date2)

            val duration = Duration.between(instant1, instant2).abs()

            val totalMinutes = duration.toMinutes()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            return String.format(Locale.getDefault(), "%d:%02d", hours, minutes)
        }

        fun addHours(timestamp: Long, hours: Int): Long{
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            calendar.add(Calendar.HOUR, hours)

            return calendar.timeInMillis
        }

        fun mergeDateTimeUTC(utcDate: Long, hour: Int, minute: Int): Long {
            val utcInstant = Instant.ofEpochMilli(utcDate)
            val localDate = LocalDateTime.ofInstant(utcInstant, ZoneId.of("UTC")).toLocalDate()

            val localDateTime = localDate.atTime(hour, minute)

            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        fun getTodayUtcMidnight(): Long {
            val now = LocalDateTime.now()
            return now.toLocalDate()
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }

        fun getUtcMidnightForLocalDate(timestamp: Long): Long {
            val localDate = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .toLocalDate()
            return localDate
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }

        fun getLocalHour(timestamp: Long): Int {
            return LocalDateTime
                .ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .hour
        }

        fun getLocalMinute(timestamp: Long): Int {
            return LocalDateTime
                .ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .minute
        }

        fun getBeginingEndOfDay(timestamp: Long): Pair<Long, Long> {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis

            return Pair(start, end)

        }

        // just for show in UI
        fun getTodayAt(hour: Int, minute: Int = 0): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

    }
}

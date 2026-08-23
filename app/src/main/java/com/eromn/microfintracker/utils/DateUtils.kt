package com.eromn.microfintracker.utils

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

/**
 * Utility methods for date and time conversions and formatting used across the app.
 */
class DateUtils {
    companion object {
        private val mxLocal = Locale.Builder().setLanguage("es").setRegion("MX").build()

        /**
         * Formats a timestamp (milliseconds) as an ISO 8601 date-time string in UTC,
         * always including milliseconds. Example: "2026-08-21T04:52:00.000Z".
         */
        fun formatIso8601Utc(timestamp: Long): String {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).format(formatter)
        }

        /**
         * Formats a timestamp (milliseconds) as a local time string using the Spanish locale.
         * Example: "3:45 p. m.".
         */
        fun formatTime(timestamp: Long): String {
            val instant = Instant.ofEpochMilli(timestamp)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

            val formatter = DateTimeFormatter.ofPattern("h:mm a", mxLocal)
            return localDateTime.format(formatter)
        }

        /**
         * Formats an hour and minute into a local time string using [formatTime].
         */
        fun formatTime(hour: Int, minute: Int): String {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.isLenient = false

            return formatTime(cal.timeInMillis )
        }

        /**
         * Formats a UTC-based timestamp (milliseconds) as a date string in Spanish.
         * Example: "15 de jul.".
         */
        fun formatDateUTC(timestamp: Long): String {
            val instant = Instant.ofEpochMilli(timestamp)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))

            val formatter = DateTimeFormatter.ofPattern("d 'de' MMM", mxLocal)
            return localDateTime.format(formatter)
        }

        /**
         * Returns a human-friendly header label for a timestamp relative to today.
         * Possible outputs include "Hoy", "Ayer, 15 de jul.", "Ante ayer, ...", or the full date.
         */
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

        /**
         * Returns the absolute duration between two timestamps formatted as "H:mm".
         */
        fun timeBetween(date1: Long, date2: Long): String {
            val instant1 = Instant.ofEpochMilli(date1)
            val instant2 = Instant.ofEpochMilli(date2)

            val duration = Duration.between(instant1, instant2).abs()

            val totalMinutes = duration.toMinutes()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            return String.format(Locale.getDefault(), "%d:%02d", hours, minutes)
        }

        /**
         * Adds the given number of hours to the provided timestamp.
         */
        fun addHours(timestamp: Long, hours: Int): Long{
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            calendar.add(Calendar.HOUR, hours)

            return calendar.timeInMillis
        }

        /**
         * Combines a UTC date (milliseconds at UTC midnight) with a local hour/minute
         * and returns the correct epoch milliseconds in the device's time zone.
         */
        fun mergeDateTimeUTC(utcDate: Long, hour: Int, minute: Int): Long {
            val utcInstant = Instant.ofEpochMilli(utcDate)
            val localDate = LocalDateTime.ofInstant(utcInstant, ZoneId.of("UTC")).toLocalDate()

            val localDateTime = localDate.atTime(hour, minute)

            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        /**
         * Returns today's midnight timestamp in UTC.
         */
        fun getTodayUtcMidnight(): Long {
            val now = LocalDateTime.now()
            return now.toLocalDate()
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }

        /**
         * Returns the UTC midnight timestamp corresponding to the local date of the given timestamp.
         */
        fun getUtcMidnightForLocalDate(timestamp: Long): Long {
            val localDate = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .toLocalDate()
            return localDate
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }

        /**
         * Returns the hour component of the given timestamp in the local time zone.
         */
        fun getLocalHour(timestamp: Long): Int {
            return LocalDateTime
                .ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .hour
        }

        /**
         * Returns the minute component of the given timestamp in the local time zone.
         */
        fun getLocalMinute(timestamp: Long): Int {
            return LocalDateTime
                .ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .minute
        }

        /**
         * Returns the start and end of the local day for the given timestamp, as a pair of
         * epoch milliseconds representing 00:00:00.000 and 23:59:59.999 on that day.
         */
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

        /**
         * Returns the epoch milliseconds for today at the provided local hour and minute.
         */
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

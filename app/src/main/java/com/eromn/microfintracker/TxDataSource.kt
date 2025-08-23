package com.eromn.microfintracker

import android.content.Context
import android.content.Context.MODE_APPEND
import java.io.FileNotFoundException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Trip(val timestamp: Long, val type: String)

class TxDataSource(private val context: Context) {

    fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val mxLocal = Locale.Builder().setLanguage("es").setRegion("MX").build()

        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", mxLocal)
        return localDateTime.format(formatter)
    }

    fun logTransaction(type: String) {
        val timestamp = System.currentTimeMillis()
        val line = "$timestamp,$type\n"
        context.openFileOutput("transport_log.csv", MODE_APPEND).use { output ->
            output.write(line.toByteArray())
        }
    }

    fun readTransactions(): List<Trip> {
        return try {
            context.openFileInput("transport_log.csv")
                .bufferedReader()
                .readLines()
                .mapNotNull { line ->
                    val parts = line.split(",")
                    if (parts.size == 2) {
                        val time = parts[0].toLongOrNull()
                        val type = parts[1]
                        if (time != null) Trip(time, type) else null
                    } else null
                }
        } catch (e: FileNotFoundException) {
            emptyList()
        }
    }
}
package com.eromn.microfintracker

import android.content.Context
import android.content.Context.MODE_APPEND
import java.io.FileNotFoundException

data class Trip(val timestamp: Long, val type: String)

class TxDataSource(private val context: Context) {

    fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
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
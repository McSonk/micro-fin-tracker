package com.eromn.microfintracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Transaction (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String = "",
    // TODO: Change to BigDecimal
    val amount: Double = 0.0,
    val timestamp: Long = 0,
    @ColumnInfo(name = "is_read", defaultValue = "0")
    val isRead: Boolean = false
)
package com.eromn.microfintracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name="description")
    val description: String = "",
    // TODO: Change to BigDecimal
    @ColumnInfo(name="amount")
    val amount: Double = 0.0,
    @ColumnInfo(name="timestamp")
    val timestamp: Long = 0,
)
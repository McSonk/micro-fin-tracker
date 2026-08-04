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
    val isRead: Boolean = false,
    @ColumnInfo(name = "category_id")
    val categoryId: Int? = 0
)

enum class LocalCategory(val serverId: Int, val displayName: String) {
    UNCATEGORIZED(0, "Uncategorized"),
    FOOD(1, "Food & Dining"),
    TRANSPORT(2, "Transport"),
    INCOME(3, "Income");

    companion object {
        // Helper to map the DB integer to your UI Enum
        fun fromId(id: Int?): LocalCategory =
            entries.firstOrNull { it.serverId == id } ?: UNCATEGORIZED
    }
}
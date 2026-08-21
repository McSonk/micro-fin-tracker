package com.eromn.microfintracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [Transaction] entities.
 */
@Dao
interface TransactionDao {

    /**
     * Observes all transactions, ordered by timestamp descending, then id descending.
     */
    @Query("SELECT * FROM `transaction` ORDER BY timestamp DESC, id DESC")
    fun getAll(): Flow<List<Transaction>>

    /**
     * Inserts a new transaction into the database.
     */
    @Insert
    suspend fun insert(transaction: Transaction)

    /**
     * Updates an existing transaction in the database.
     */
    @Update
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Deletes the given transaction from the database.
     */
    @Delete
    suspend fun delete(transaction: Transaction)

    /**
     * Deletes a transaction by its id.
     */
    @Query("DELETE FROM `transaction` WHERE id = :id")
    suspend fun deleteById(id: Int)
}

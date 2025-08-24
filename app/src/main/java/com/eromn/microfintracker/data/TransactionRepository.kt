package com.eromn.microfintracker.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAll()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteById(id: Int) {
        transactionDao.deleteById(id)
    }

}
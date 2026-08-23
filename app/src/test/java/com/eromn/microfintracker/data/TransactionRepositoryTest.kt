package com.eromn.microfintracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests [TransactionRepository.deleteRead] against an in-memory [TransactionDao].
 */
class TransactionRepositoryTest {

    @Test
    fun deleteRead_removesOnlyReadTransactions() = runBlocking {
        val dao = FakeTransactionDao()
        dao.seed(Transaction(id = 1, description = "read 1", isRead = true))
        dao.seed(Transaction(id = 2, description = "read 2", isRead = true))
        dao.seed(Transaction(id = 3, description = "unread 1", isRead = false))
        dao.seed(Transaction(id = 4, description = "unread 2", isRead = false))
        val repository = TransactionRepository(dao)

        val count = repository.deleteRead()

        assertEquals(2, count)
        val remaining = dao.storedTransactions()
        assertEquals(2, remaining.size)
        assertTrue(remaining.none { it.isRead })
        assertEquals(listOf(3, 4), remaining.map { it.id })
    }

    @Test
    fun deleteRead_noReadTransactions_returnsZero() = runBlocking {
        val dao = FakeTransactionDao()
        dao.seed(Transaction(id = 1, description = "unread", isRead = false))
        val repository = TransactionRepository(dao)

        val count = repository.deleteRead()

        assertEquals(0, count)
        assertEquals(1, dao.storedTransactions().size)
    }

    /**
     * In-memory implementation of [TransactionDao] used to observe writes without a real DB.
     */
    private class FakeTransactionDao : TransactionDao {
        private val _transactions = mutableListOf<Transaction>()
        private val _allFlow = MutableStateFlow<List<Transaction>>(emptyList())

        fun seed(transaction: Transaction) {
            _transactions.add(transaction)
            _allFlow.value = _transactions.toList()
        }

        fun storedTransactions(): List<Transaction> = _transactions.toList()

        override fun getAll(): Flow<List<Transaction>> = _allFlow

        override suspend fun getUnread(): List<Transaction> =
            _transactions.filter { !it.isRead }

        override suspend fun insert(transaction: Transaction) {
            _transactions.add(transaction)
            _allFlow.value = _transactions.toList()
        }

        override suspend fun updateTransaction(transaction: Transaction) {
            val index = _transactions.indexOfFirst { it.id == transaction.id }
            if (index >= 0) _transactions[index] = transaction
            _allFlow.value = _transactions.toList()
        }

        override suspend fun delete(transaction: Transaction) {
            _transactions.removeAll { it.id == transaction.id }
            _allFlow.value = _transactions.toList()
        }

        override suspend fun deleteById(id: Int) {
            _transactions.removeAll { it.id == id }
            _allFlow.value = _transactions.toList()
        }

        override suspend fun deleteRead(): Int {
            val removed = _transactions.filter { it.isRead }
            _transactions.removeAll { it.isRead }
            _allFlow.value = _transactions.toList()
            return removed.size
        }
    }
}
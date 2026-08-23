package com.eromn.microfintracker.data.repository

import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.data.TransactionDao
import com.eromn.microfintracker.data.remote.TransactionApi
import com.eromn.microfintracker.data.remote.TransactionDto
import com.eromn.microfintracker.domain.model.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Tests [TransactionUploadRepositoryImpl] against a [MockWebServer], without any real
 * network call. Uses an in-memory [TransactionDao] as the database substitute.
 */
class TransactionUploadRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun uploadPendingTransactions_success_marksAsReadInDatabase() = runBlocking {
        val localTransaction = Transaction(
            id = 1,
            description = "Test transaction from android",
            amount = 5000.0,
            timestamp = Instant.parse("2026-08-21T04:52:00.000Z").toEpochMilli(),
            isRead = false,
            categoryId = 4
        )
        val dao = FakeTransactionDao()
        dao.seed(localTransaction)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "id": 100,
                        "user_id": 1,
                        "date": "2026-08-21T04:52:00.000Z",
                        "timezone": "Asia/Taipei",
                        "amount": 5000.0,
                        "amount_currency_id": 2,
                        "amount_in_local_currency": 5000.0,
                        "description": "Test transaction from android",
                        "account_id": 4,
                        "category_id": 4,
                        "extra_field_ignored": true
                    }
                    """
                )
        )

        val api = TransactionApi.create(baseUrl = mockWebServer.url("/").toString())
        val repository = TransactionUploadRepositoryImpl(dao, api)

        val result = repository.uploadPendingTransactions()

        val request = mockWebServer.takeRequest()
        assertEquals("/transactions", request.path)
        assertEquals("POST", request.method)
        assertTrue(request.getHeader("Content-Type")!!.startsWith("application/json"))

        val sent = TransactionApi.json.decodeFromString<TransactionDto>(request.body.readUtf8())
        assertEquals("2026-08-21T04:52:00.000Z", sent.date)
        assertEquals("Test transaction from android", sent.description)
        assertEquals(5000.0, sent.amount, 0.0)
        assertEquals(4L, sent.categoryId)
        assertEquals(1L, sent.userId)
        assertEquals("Asia/Taipei", sent.timezone)
        assertEquals(4L, sent.accountId)
        assertEquals(2L, sent.amountCurrencyId)
        assertEquals(null, sent.id)
        assertEquals(null, sent.amountInLocalCurrency)

        assertEquals(UploadResult.Summary(1, 0), result)
        assertTrue(dao.storedTransactions().single().isRead)
    }

    @Test
    fun uploadPendingTransactions_noPending_returnsNoPending() = runBlocking {
        val dao = FakeTransactionDao()
        val api = TransactionApi.create(baseUrl = mockWebServer.url("/").toString())
        val repository = TransactionUploadRepositoryImpl(dao, api)

        val result = repository.uploadPendingTransactions()

        assertEquals(UploadResult.NoPendingTransactions, result)
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
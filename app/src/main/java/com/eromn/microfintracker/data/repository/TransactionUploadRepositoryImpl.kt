package com.eromn.microfintracker.data.repository

import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.data.TransactionDao
import com.eromn.microfintracker.data.remote.TransactionApi
import com.eromn.microfintracker.data.remote.TransactionDto
import com.eromn.microfintracker.domain.model.UploadResult
import com.eromn.microfintracker.domain.repository.TransactionUploadRepository
import com.eromn.microfintracker.utils.DateUtils
import kotlinx.coroutines.CancellationException

/**
 * Uploads pending transactions using [TransactionApi] and marks only successfully
 * uploaded ones as read. Failures do not stop the batch; they are counted and reported.
 */
class TransactionUploadRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val transactionApi: TransactionApi
) : TransactionUploadRepository {

    override suspend fun uploadPendingTransactions(): UploadResult {
        val pending = transactionDao.getUnread()
        if (pending.isEmpty()) return UploadResult.NoPendingTransactions

        var successCount = 0
        var errorCount = 0
        for (transaction in pending) {
            try {
                transactionApi.createTransaction(transaction.toDto())
                transactionDao.updateTransaction(transaction.copy(isRead = true))
                successCount++
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorCount++
            }
        }
        return UploadResult.Summary(successCount, errorCount)
    }

    /**
     * Maps this local [Transaction] to the wire [TransactionDto]. Server-side defaults are
     * applied here: timezone, account, currency, and the mock user id.
     */
    private fun Transaction.toDto(): TransactionDto {
        return TransactionDto(
            userId = MOCK_USER_ID,
            date = DateUtils.formatIso8601Utc(timestamp),
            timezone = TIMEZONE,
            amount = amount,
            amountCurrencyId = CURRENCY_ID,
            description = description,
            accountId = ACCOUNT_ID,
            categoryId = categoryId?.toLong()
        )
    }

    private companion object {
        const val MOCK_USER_ID = 1L
        const val ACCOUNT_ID = 4L
        const val CURRENCY_ID = 2L
        const val TIMEZONE = "Asia/Taipei"
    }
}
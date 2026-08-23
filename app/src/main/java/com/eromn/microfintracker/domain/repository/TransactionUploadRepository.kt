package com.eromn.microfintracker.domain.repository

import com.eromn.microfintracker.domain.model.UploadResult

/**
 * Uploads locally pending transactions to the server.
 */
interface TransactionUploadRepository {

    /**
     * Uploads all transactions marked as unread and returns a summary of the outcome.
     * Only successfully uploaded transactions are marked as read.
     */
    suspend fun uploadPendingTransactions(): UploadResult
}
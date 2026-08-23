package com.eromn.microfintracker.domain.model

/**
 * Outcome of a transaction upload attempt, surfaced to the UI for feedback.
 */
sealed interface UploadResult {

    /**
     * There were no pending (unread) transactions to upload.
     */
    data object NoPendingTransactions : UploadResult

    /**
     * A batch upload finished with the given number of successes and failures.
     */
    data class Summary(
        val successCount: Int,
        val errorCount: Int
    ) : UploadResult

    /**
     * The upload attempt failed before any batch result could be produced.
     */
    data object Failed : UploadResult
}
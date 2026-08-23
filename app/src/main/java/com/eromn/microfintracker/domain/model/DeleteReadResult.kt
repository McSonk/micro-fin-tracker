package com.eromn.microfintracker.domain.model

/**
 * Outcome of deleting all read (already-uploaded) transactions, surfaced to the UI for feedback.
 */
sealed interface DeleteReadResult {

    /**
     * There were no read transactions to delete.
     */
    data object NoReadTransactions : DeleteReadResult

    /**
     * A deletion finished with the given number of removed transactions.
     */
    data class Deleted(
        val count: Int
    ) : DeleteReadResult

    /**
     * The deletion attempt failed.
     */
    data object Failed : DeleteReadResult
}
package com.eromn.microfintracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eromn.microfintracker.data.Transaction
import androidx.lifecycle.viewModelScope
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for the history screen. Exposes the transaction list grouped by header date,
 * handles CRUD operations, and manages the state of the transaction being edited.
 */
class HistoryViewModel(private val repository: TransactionRepository) : ViewModel() {

    /**
     * All transactions from the repository as a Flow.
     */
    val allTransactions: Flow<List<Transaction>> = repository.allTransactions

    /**
     * Transactions grouped by header label (e.g. "Hoy", "Ayer", full date).
     */
    val groupedTransactions: Flow<Map<String, List<Transaction>>> = allTransactions
        .map { list->
            list.groupBy { DateUtils.formatHeaderDate(it.timestamp) }
        }
        .flowOn(Dispatchers.Default)

    private val _editingTransaction = MutableStateFlow<Transaction?>(null)

    /**
     * The transaction currently being edited, if any.
     */
    val editingTransaction: StateFlow<Transaction?> = _editingTransaction.asStateFlow()

    private val _saveFailed = MutableStateFlow(false)

    /**
     * True if the last save operation failed.
     */
    val saveFailed: StateFlow<Boolean> = _saveFailed.asStateFlow()

    /**
     * Logs a new transaction with the given description, amount, and optional timestamp.
     */
    fun logTransaction(
        description: String,
        amount: Double,
        timestamp: Long = System.currentTimeMillis()
    ) = viewModelScope.launch(Dispatchers.IO) {
        val newTransaction = Transaction(
            description = description,
            amount = amount,
            timestamp = timestamp
        )
        repository.insert(newTransaction)
    }

    /**
     * Marks the given transaction as read.
     */
    fun markAsRead(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        val updatedTransaction = transaction.copy(isRead = true)
        repository.updateTransaction(updatedTransaction)
    }

    /**
     * Marks the given transaction as unread.
     */
    fun markAsUnread(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        val updatedTransaction = transaction.copy(isRead = false)
        repository.updateTransaction(updatedTransaction)
    }

    /**
     * Deletes the given transaction.
     */
    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(transaction)
    }

    /**
     * Starts editing the provided transaction; resets any previous save error.
     */
    fun startEditing(transaction: Transaction) {
        _saveFailed.value = false
        _editingTransaction.value = transaction
    }

    /**
     * Clears the current editing state and any save error.
     */
    fun clearEditing() {
        _editingTransaction.value = null
        _saveFailed.value = false
    }

    /**
     * Clears only the save error flag.
     */
    fun clearSaveError() {
        _saveFailed.value = false
    }

    /**
     * Inserts a new transaction (id == 0) or updates an existing one.
     * Sets [saveFailed] to true if the operation throws an exception.
     */
    fun saveTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (transaction.id == 0) {
                repository.insert(transaction)
            } else {
                repository.updateTransaction(transaction)
            }
            _editingTransaction.value = null
            _saveFailed.value = false
        } catch (e: Exception) {
            _saveFailed.value = true
        }
    }
}

/**
 * Factory to create [HistoryViewModel] with a manually injected repository.
 */
class HistoryViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of [HistoryViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

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

class HistoryViewModel(private val repository: TransactionRepository) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = repository.allTransactions
    val groupedTransactions: Flow<Map<String, List<Transaction>>> = allTransactions
        .map { list->
            list.groupBy { DateUtils.formatHeaderDate(it.timestamp) }
        }
        .flowOn(Dispatchers.Default)

    private val _editingTransaction = MutableStateFlow<Transaction?>(null)
    val editingTransaction: StateFlow<Transaction?> = _editingTransaction.asStateFlow()

    private val _saveFailed = MutableStateFlow(false)
    val saveFailed: StateFlow<Boolean> = _saveFailed.asStateFlow()

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

    fun markAsRead(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        val updatedTransaction = transaction.copy(isRead = true)
        repository.updateTransaction(updatedTransaction)
    }

    fun markAsUnread(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        val updatedTransaction = transaction.copy(isRead = false)
        repository.updateTransaction(updatedTransaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(transaction)
    }

    fun startEditing(transaction: Transaction) {
        _saveFailed.value = false
        _editingTransaction.value = transaction
    }

    fun clearEditing() {
        _editingTransaction.value = null
        _saveFailed.value = false
    }

    fun clearSaveError() {
        _saveFailed.value = false
    }

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

// ViewModel Factory to allow passing Repository to ViewModel constructor
class HistoryViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

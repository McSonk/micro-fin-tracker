package com.eromn.microfintracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eromn.microfintracker.data.Transaction
import androidx.lifecycle.viewModelScope
import com.eromn.microfintracker.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: TransactionRepository) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = repository.allTransactions

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
        repository.update(updatedTransaction)
    }

    fun markAsUnread(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        val updatedTransaction = transaction.copy(isRead = false)
        repository.update(updatedTransaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(transaction)
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
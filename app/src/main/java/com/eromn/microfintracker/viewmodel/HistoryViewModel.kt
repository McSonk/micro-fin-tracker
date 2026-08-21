package com.eromn.microfintracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eromn.microfintracker.data.Category
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.domain.repository.CategoryRepository
import com.eromn.microfintracker.utils.DateUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * ViewModel for the history screen. Exposes the transaction list grouped by header date,
 * handles CRUD operations, and manages the state of the transaction being edited.
 */
class HistoryViewModel(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    /**
     * All transactions from the repository as a Flow.
     */
    val allTransactions: Flow<List<Transaction>> = repository.allTransactions

    /**
     * Transactions grouped by header label (e.g. "Hoy", "Ayer", full date).
     */
    val groupedTransactions: Flow<Map<String, List<Transaction>>> = allTransactions
        .map { list ->
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

    // Category picker state
    private val _categoryOptions = MutableStateFlow<List<Category>>(emptyList())
    val categoryOptions: StateFlow<List<Category>> = _categoryOptions.asStateFlow()

    private val _categorySearchQuery = MutableStateFlow("")
    val categorySearchQuery: StateFlow<String> = _categorySearchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow(Category.OTHERS.serverId)
    val selectedCategory: StateFlow<Category> = _selectedCategoryId
        .map { Category.fromId(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Category.OTHERS
        )

    private val _isCategoryPickerVisible = MutableStateFlow(false)
    val isCategoryPickerVisible: StateFlow<Boolean> = _isCategoryPickerVisible.asStateFlow()

    val filteredCategoryOptions: StateFlow<List<Category>> = combine(
        _categoryOptions,
        _categorySearchQuery
    ) { options, query ->
        if (query.isBlank()) options
        else options.filter { it.displayName.contains(query.trim(), ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            _categoryOptions.value = categoryRepository.getCategories()
        }
    }

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

    fun restoreTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(transaction.copy(id = 0))
    }

    fun startAdding() {
        _editingTransaction.value = null
        _selectedCategoryId.value = Category.OTHERS.serverId
        _isCategoryPickerVisible.value = false
        _categorySearchQuery.value = ""
        _saveFailed.value = false
    }

    /**
     * Starts editing the provided transaction; resets any previous save error.
     */
    fun startEditing(transaction: Transaction) {
        _saveFailed.value = false
        _editingTransaction.value = transaction
        _selectedCategoryId.value = transaction.categoryId ?: Category.OTHERS.serverId
        _isCategoryPickerVisible.value = false
        _categorySearchQuery.value = ""
    }

    /**
     * Clears the current editing state and any save error.
     */
    fun clearEditing() {
        _editingTransaction.value = null
        _saveFailed.value = false
        _selectedCategoryId.value = Category.OTHERS.serverId
        _isCategoryPickerVisible.value = false
        _categorySearchQuery.value = ""
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
            val normalizedTransaction = transaction.copy(
                categoryId = transaction.categoryId ?: _selectedCategoryId.value
            )
            if (normalizedTransaction.id == 0) {
                repository.insert(normalizedTransaction)
            } else {
                repository.updateTransaction(normalizedTransaction)
            }
            _editingTransaction.value = null
            _saveFailed.value = false
        } catch (e: Exception) {
            if(e is CancellationException){
                // If the user switched screen on purpose
                throw e
            }
            _saveFailed.value = true
        }
    }

    // Category picker actions
    fun showCategoryPicker() {
        _isCategoryPickerVisible.value = true
        _categorySearchQuery.value = ""
    }

    fun dismissCategoryPicker() {
        _isCategoryPickerVisible.value = false
        _categorySearchQuery.value = ""
    }

    fun onCategorySearchQueryChanged(query: String) {
        _categorySearchQuery.value = query
    }

    fun selectCategory(category: Category) {
        _selectedCategoryId.value = category.serverId
        _isCategoryPickerVisible.value = false
        _categorySearchQuery.value = ""
    }
}

/**
 * Factory to create [HistoryViewModel] with a manually injected repository.
 */
class HistoryViewModelFactory(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of [HistoryViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository, categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

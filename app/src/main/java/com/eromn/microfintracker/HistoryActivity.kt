package com.eromn.microfintracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.eromn.microfintracker.data.AppDatabase
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.data.remote.TransactionApi
import com.eromn.microfintracker.data.repository.CategoryRepositoryImpl
import com.eromn.microfintracker.data.repository.TransactionUploadRepositoryImpl
import com.eromn.microfintracker.ui.screens.DashboardScreen
import com.eromn.microfintracker.viewmodel.HistoryViewModel
import com.eromn.microfintracker.viewmodel.HistoryViewModelFactory

/**
 * Activity that hosts the history dashboard screen and wires it to [HistoryViewModel].
 */
class HistoryActivity : AppCompatActivity() {
    private val historyViewModel: HistoryViewModel by viewModels {
        val transactionDao = AppDatabase.getDatabase(applicationContext).transactionDao()
        HistoryViewModelFactory(
            TransactionRepository(transactionDao),
            CategoryRepositoryImpl(),
            TransactionUploadRepositoryImpl(
                transactionDao,
                TransactionApi.create()
            )
        )
    }

    /**
     * Called when the activity is starting. Sets up the Compose UI and observes the ViewModel state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val transactions by historyViewModel.groupedTransactions.collectAsStateWithLifecycle(initialValue = emptyMap())
            val editingTransaction by historyViewModel.editingTransaction.collectAsStateWithLifecycle()
            val saveFailed by historyViewModel.saveFailed.collectAsStateWithLifecycle()
            val filteredCategories by historyViewModel.filteredCategoryOptions.collectAsStateWithLifecycle()
            val selectedCategory by historyViewModel.selectedCategory.collectAsStateWithLifecycle()
            val isCategoryPickerVisible by historyViewModel.isCategoryPickerVisible.collectAsStateWithLifecycle()
            val categorySearchQuery by historyViewModel.categorySearchQuery.collectAsStateWithLifecycle()
            val uploadResult by historyViewModel.uploadResult.collectAsStateWithLifecycle()
            val isUploading by historyViewModel.isUploading.collectAsStateWithLifecycle()

            LaunchedEffect(saveFailed) {
                if (saveFailed) {
                    Toast.makeText(
                        this@HistoryActivity,
                        getString(R.string.save_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    historyViewModel.clearSaveError()
                }
            }

            DashboardScreen(
                username = "McSonk",
                monthlySpent = 950.0,
                todaySpent = 500.0,
                transactionsByDate = transactions,
                editingTransaction = editingTransaction,
                onUploadToServer = { historyViewModel.uploadToServer() },
                isUploading = isUploading,
                uploadResult = uploadResult,
                onUploadResultShown = { historyViewModel.clearUploadResult() },
                onSaveTransaction = { transaction ->
                    historyViewModel.saveTransaction(transaction)
                },
                onTransactionClick = { transaction ->
                    historyViewModel.startEditing(transaction)
                },
                onDeleteTransaction = { tx -> historyViewModel.deleteTransaction(tx) },
                onUndoDelete = { tx ->
                    historyViewModel.restoreTransaction(tx)
                    Toast.makeText(this@HistoryActivity, "Transaction restored!", Toast.LENGTH_SHORT).show()
                },
                onDismissTransactionSheet = {
                    historyViewModel.clearEditing()
                },
                selectedCategory = selectedCategory,
                isCategoryPickerVisible = isCategoryPickerVisible,
                categorySearchQuery = categorySearchQuery,
                filteredCategories = filteredCategories,
                onAddTransactionRequested = { historyViewModel.startAdding() },
                onCategoryFieldClicked = { historyViewModel.showCategoryPicker() },
                onCategorySearchQueryChanged = { historyViewModel.onCategorySearchQueryChanged(it) },
                onCategorySelected = { historyViewModel.selectCategory(it) },
                onDismissCategoryPicker = { historyViewModel.dismissCategoryPicker() }
            )
        }
    }
}

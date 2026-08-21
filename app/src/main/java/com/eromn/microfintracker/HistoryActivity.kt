package com.eromn.microfintracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.eromn.microfintracker.data.AppDatabase
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.ui.components.DashboardScreen
import com.eromn.microfintracker.viewmodel.HistoryViewModel
import com.eromn.microfintracker.viewmodel.HistoryViewModelFactory

/**
 * Activity that hosts the history dashboard screen and wires it to [HistoryViewModel].
 */
class HistoryActivity : AppCompatActivity() {
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(applicationContext).transactionDao())
        )
    }

    /**
     * Called when the activity is starting. Sets up the Compose UI and observes the ViewModel state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val transactions by historyViewModel.groupedTransactions.collectAsState(emptyMap())
            val editingTransaction by historyViewModel.editingTransaction.collectAsState()
            val saveFailed by historyViewModel.saveFailed.collectAsState()

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
                onLogout = {},
                onSaveTransaction = { transaction ->
                    historyViewModel.saveTransaction(transaction)
                },
                onTransactionClick = { transaction ->
                    historyViewModel.startEditing(transaction)
                },
                onDeleteTransaction = { tx -> historyViewModel.deleteTransaction(tx) },
                onUndoDelete = { tx ->
                    historyViewModel.logTransaction(
                        tx.description,
                        tx.amount,
                        tx.timestamp
                    )
                    Toast.makeText(this@HistoryActivity, "Transaction restored!", Toast.LENGTH_SHORT).show()
                },
                onDismissTransactionSheet = {
                    historyViewModel.clearEditing()
                }
            )
        }
    }
}

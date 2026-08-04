package com.eromn.microfintracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.eromn.microfintracker.data.AppDatabase
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.ui.components.DashboardScreen
import com.eromn.microfintracker.viewmodel.HistoryViewModel
import com.eromn.microfintracker.viewmodel.HistoryViewModelFactory

class HistoryActivity : AppCompatActivity() {
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(applicationContext).transactionDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Retrieve transactions from db
            val transactions by historyViewModel.groupedTransactions.collectAsState(emptyMap())
            // for the "undo" action
            val scope = rememberCoroutineScope()
            /*HistoryMainCanvas(
                transactions,
                snackbarHostState,
                onToggleRead = { tx -> toggleRead(tx) },
                onDelete = { tx -> deleteTransaction(tx, scope, snackbarHostState) },
                onSave = { desc, amount, timestamp ->
                    historyViewModel.logTransaction(desc, amount, timestamp)
                }
            )*/

            DashboardScreen(
                username = "McSonk",           // Later: collect from ViewModel
                monthlySpent = 950.0,          // Later: collect from ViewModel
                todaySpent = 500.0,            // Later: collect from ViewModel
                transactionsByDate = transactions,
                onLogout = {},
                onAddTransaction = {},
                onTransactionClick = {},
                onDeleteTransaction = { tx -> historyViewModel.deleteTransaction(tx) },
                onUndoDelete = { tx ->
                    historyViewModel.logTransaction(
                        tx.description,
                        tx.amount,
                        tx.timestamp
                    )
                    Toast.makeText(this@HistoryActivity, "Transaction restored!", Toast.LENGTH_SHORT).show()
                }
            )
        }//end setContent
    }// end onCreate

}// end class

package com.eromn.microfintracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.eromn.microfintracker.data.AppDatabase
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.ui.components.HistoryMainCanvas
import com.eromn.microfintracker.viewmodel.HistoryViewModel
import com.eromn.microfintracker.viewmodel.HistoryViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            HistoryMainCanvas(
                transactions,
                snackbarHostState,
                onToggleRead = { tx -> toggleRead(tx) },
                onDelete = { tx -> deleteTransaction(tx, scope, snackbarHostState) },
                onSave = { desc, amount, timestamp ->
                    historyViewModel.logTransaction(desc, amount, timestamp)
                }
            )
        }//end setContent
    }// end onCreate

    fun deleteTransaction(
        tx: Transaction,
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState
    ) {
        historyViewModel.deleteTransaction(tx)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Deleted: ${tx.description}",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                historyViewModel.logTransaction(
                    tx.description,
                    tx.amount,
                    tx.timestamp
                )
                Toast.makeText(this@HistoryActivity, "Transaction restored!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleRead(tx: Transaction) {
        if (!tx.isRead) { // Only mark as read if it's currently unread
            historyViewModel.markAsRead(tx)
        } else {
            historyViewModel.markAsUnread(tx)
        }// end if-else isRead
    }
    
}// end class

package com.eromn.microfintracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eromn.microfintracker.data.AppDatabase
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.databinding.ActivityHistoryBinding
import com.eromn.microfintracker.viewmodel.HistoryViewModel
import com.eromn.microfintracker.viewmodel.HistoryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(applicationContext).transactionDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize adapter with an empty list and the delete listener
        // The list will be populated by observing the ViewModel
        historyAdapter = HistoryAdapter(mutableListOf())
        binding.recyclerHistory.adapter = historyAdapter
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            historyViewModel.allTransactions.collectLatest { transactions ->
                // Update the adapter with the new list of trips
                // Consider using DiffUtil for better performance if lists are large

                historyAdapter.updateTransactions(transactions)
            }
        }

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0,  // We are not implementing drag and drop, so 0.
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) : Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // This method is called when an item is fully swiped.
                // The 'direction' parameter will tell you which way it was swiped.

                if (direction == ItemTouchHelper.LEFT) {
                    val position = viewHolder.adapterPosition
                    val transactionToDelete = historyAdapter.transactions[position]

                    // 1. Tell the ViewModel to delete the transaction
                    historyViewModel.deleteTransaction(transactionToDelete)

                    // 2. Show a Snackbar for feedback (and optional UNDO)
                    Snackbar.make(binding.root, "Deleted: ${transactionToDelete.description}", Snackbar.LENGTH_LONG)
                        .setAction("UNDO"){
                            // To UNDO, we re-insert the transaction.
                            historyViewModel.logTransaction(
                                transactionToDelete.description,
                                transactionToDelete.amount,
                                transactionToDelete.timestamp
                            )
                            Toast.makeText(this@HistoryActivity, "Transaction restored!", Toast.LENGTH_SHORT).show()
                        }
                        .show()

                    Toast.makeText(
                        this@HistoryActivity,
                        "Transaction deleted!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (direction == ItemTouchHelper.RIGHT) {
                    val position = viewHolder.adapterPosition
                    // IMPORTANT: To make the item reappear after swipe (since we are not deleting yet),
                    // we need to notify the adapter that the item at this position has changed.
                    // This will trigger a rebind and reset its position.
                    historyAdapter.notifyItemChanged(position)
                }// end if
            }// end onSwiped
        }// end simpleItemTouchCallback

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerHistory)
    }
}

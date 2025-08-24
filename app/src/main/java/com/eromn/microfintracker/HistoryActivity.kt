package com.eromn.microfintracker

import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

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
                val position = viewHolder.bindingAdapterPosition // Use bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return // Item likely already removed or not bound
                }
                val transaction = historyAdapter.transactions[position]

                // The 'direction' parameter will tell you which way it was swiped.
                if (direction == ItemTouchHelper.LEFT) {
                    // 1. Tell the ViewModel to delete the transaction
                    historyViewModel.deleteTransaction(transaction)

                    // 2. Show a Snackbar for feedback (and optional UNDO)
                    Snackbar.make(binding.root, "Deleted: ${transaction.description}", Snackbar.LENGTH_LONG)
                        .setAction("UNDO"){
                            // To UNDO, we re-insert the transaction.
                            historyViewModel.logTransaction(
                                transaction.description,
                                transaction.amount,
                                transaction.timestamp
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
                    if (!transaction.isRead) { // Only mark as read if it's currently unread
                        historyViewModel.markAsRead(transaction)
                        Snackbar.make(
                            binding.root,
                            "${transaction.description} marked as read",
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("UNDO") {
                                historyViewModel.markAsUnread(transaction)
                                // The UI will update automatically due to Flow/LiveData observation
                            }
                            .show()
                    } else {
                        historyViewModel.markAsUnread(transaction)
                        Snackbar.make(
                            binding.root,
                            "${transaction.description} marked as unread",
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("UNDO") {
                                historyViewModel.markAsRead(transaction)
                            }
                            .show()
                    }// end if-else isRead
                }// end if-else direction
            }// end onSwiped

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, // How much the item swiped horiz
                dy: Float, // .... vertical (0 for horizontal swipes)
                actionState: Int,
                isCurrentlyActive: Boolean
            ){
                // Use the RecyclerViewSwipeDecorator for easy styling
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dy,
                    actionState,
                    isCurrentlyActive
                )
                    // Swipe Left
                    .addSwipeLeftBackgroundColor(
                        ContextCompat.getColor(this@HistoryActivity, android.R.color.holo_red_dark)
                    )
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    // Swipe Right
                    .addSwipeRightBackgroundColor(
                        ContextCompat.getColor(this@HistoryActivity, android.R.color.holo_green_dark)
                    )
                    .addSwipeRightActionIcon(R.drawable.ic_check)
                    .create()
                    .decorate()
                // VERY IMPORTANT: Call super.onChildDraw to allow ItemTouchHelper to move the view
                super.onChildDraw(c, recyclerView, viewHolder, dX, dy, actionState, isCurrentlyActive)
            }// end onChildDraw
        }// end simpleItemTouchCallback

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerHistory)
    }
}

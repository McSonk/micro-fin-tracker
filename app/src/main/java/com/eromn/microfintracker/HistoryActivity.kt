package com.eromn.microfintracker

import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eromn.microfintracker.data.AppDatabase
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.databinding.ActivityHistoryBinding
import com.eromn.microfintracker.ui.theme.FinTrackTheme
import com.eromn.microfintracker.utils.DateUtils
import com.eromn.microfintracker.viewmodel.HistoryViewModel
import com.eromn.microfintracker.viewmodel.HistoryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private val dateUtils = DateUtils()

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

        binding.composeView.setContent {
            //temporarily here
            // Retrieve transactions from db
            val transactions by historyViewModel.allTransactions.collectAsState(initial = emptyList())
            FinTrackTheme() {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars) // handle system bars
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainCanvas(transactions)
                }
            }
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
                val transaction = historyAdapter.currentList[position]

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

    fun deleteTransaction(tx: Transaction) {
        historyViewModel.deleteTransaction(tx)
        Snackbar.make(binding.root, "Deleted: ${tx.description}", Snackbar.LENGTH_LONG)
            .setAction("UNDO"){
                // To UNDO, we re-insert the transaction.
                historyViewModel.logTransaction(
                    tx.description,
                    tx.amount,
                    tx.timestamp
                )
                Toast.makeText(this@HistoryActivity, "Transaction restored!", Toast.LENGTH_SHORT).show()
            }
            .show()

        Toast.makeText(
            this@HistoryActivity,
            "Transaction deleted!",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun toggleRead(tx: Transaction){
        if (!tx.isRead) { // Only mark as read if it's currently unread
            historyViewModel.markAsRead(tx)
            Snackbar.make(
                binding.root,
                "${tx.description} marked as read",
                Snackbar.LENGTH_LONG
            )
                .setAction("UNDO") {
                    historyViewModel.markAsUnread(tx)
                    // The UI will update automatically due to Flow/LiveData observation
                }
                .show()
        } else {
            historyViewModel.markAsUnread(tx)
            Snackbar.make(
                binding.root,
                "${tx.description} marked as unread",
                Snackbar.LENGTH_LONG
            )
                .setAction("UNDO") {
                    historyViewModel.markAsRead(tx)
                }
                .show()
        }// end if-else isRead
    }

    @Composable
    fun TransactionUI(
        transaction: Transaction,
        onToggleRead: (Transaction) -> Unit,
        onRemove: (Transaction) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        // Define the state
        val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
            confirmValueChange = {
                if (it == SwipeToDismissBoxValue.StartToEnd) {
                    onToggleRead(transaction)
                }
                else if (it == SwipeToDismissBoxValue.EndToStart) {
                    onRemove(transaction)
                }
                // Reset item when toggling done status (return to the callback)
                it != SwipeToDismissBoxValue.StartToEnd
            }
        )

        SwipeToDismissBox(
            state = swipeToDismissBoxState,
            modifier = modifier.fillMaxSize(),
            backgroundContent = {
                when (swipeToDismissBoxState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Icon(
                            painter = painterResource(if (transaction.isRead) R.drawable.ic_check else R.drawable.ic_launcher_foreground),
                            contentDescription = if (transaction.isRead) "Mark as unread" else "Mark as read",
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Blue)
                                .wrapContentSize(Alignment.CenterStart)
                                .padding(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "Remove tx",
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Red)
                                .wrapContentSize(Alignment.CenterEnd)
                                .padding(12.dp),

                        )
                    }
                    SwipeToDismissBoxValue.Settled -> {}
                }
            }
        ) {
            ListItem(
                headlineContent = { Text(
                    transaction.description,
                    // style = MaterialTheme.typography.labelLarge
                )},
                supportingContent = { Text(
                    dateUtils.formatTimestamp(transaction.timestamp),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall
                )},
                trailingContent = { Text(
                    transaction.amount.toString(),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelLarge
                )},
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        "Stock image",
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                        //.border(1.5.dp, MaterialTheme.colorScheme.primary)
                    )
                }
            )
        }

    }

    @Composable
    fun TxHistory(txs: List<Transaction>){
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(
                items = txs,
                key = { it.id }
            ){ transaction ->
                TransactionUI(
                    transaction,
                    onToggleRead = { toggleRead(it) },
                    onRemove = { deleteTransaction(it) },
                    modifier = Modifier.animateItem()
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    @Composable
    fun MainCanvas(txs: List<Transaction>){
        TxHistory(txs)
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview(){
        val samples = listOf(
            Transaction(0, "uBike", 10.0, 1234567890),
            Transaction(2, "MRT", 20.0, 1234567890),
            Transaction(3, "Señora pancakes", 50.0, 1234567890)
        )

        FinTrackTheme() {
            MainCanvas(samples)
        }
    }

}

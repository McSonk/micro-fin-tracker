package com.eromn.microfintracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eromn.microfintracker.data.AppDatabase
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.ui.theme.FinTrackTheme
import com.eromn.microfintracker.utils.DateUtils
import com.eromn.microfintracker.viewmodel.HistoryViewModel
import com.eromn.microfintracker.viewmodel.HistoryViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

class HistoryActivity : AppCompatActivity() {
    private val dateUtils = DateUtils()


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

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            FinTrackTheme() {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainCanvas(
                            transactions,
                            scope,
                            snackbarHostState
                        )
                    }// end Surface
                }// end Saffold

            }
        }

    }

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

    @Composable
    fun TransactionUI(
        transaction: Transaction,
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        modifier: Modifier = Modifier,
    ) {
        // Define the state
        val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
            confirmValueChange = { true },
            positionalThreshold = { totalDistance -> totalDistance * 0.7f }
        )

        LaunchedEffect(swipeToDismissBoxState.currentValue) {
            when (swipeToDismissBoxState.currentValue) {
                StartToEnd -> {
                    toggleRead(transaction)
                    // Reset state so it snaps back
                    swipeToDismissBoxState.snapTo(Settled)
                }
                EndToStart -> {
                    deleteTransaction(transaction, scope, snackbarHostState)
                }
                Settled -> {}
            }
        }

        BoxWithConstraints(modifier = modifier) {
            val width = constraints.maxWidth.toFloat()

            val offset = try{ swipeToDismissBoxState.requireOffset() } catch (e: Exception){ 0f }
            val threshold = 0.5f
            val fraction = (abs(offset) / (width * threshold)).coerceIn(0f, 1f)

            SwipeToDismissBox(
                state = swipeToDismissBoxState,
                backgroundContent = {
                    val direction = swipeToDismissBoxState.dismissDirection

                    //determine colour based on direction and custom fraction
                    val backgroundColour = when (direction) {
                        StartToEnd -> lerp(Color.LightGray, MaterialTheme.colorScheme.primaryContainer, fraction)
                        EndToStart -> lerp(Color.LightGray, MaterialTheme.colorScheme.errorContainer, fraction)
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColour)
                            .padding(horizontal = 20.dp),
                        contentAlignment = if (direction == StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        val icon = when (direction) {
                            StartToEnd -> if (transaction.isRead) R.drawable.ic_check else R.drawable.ic_launcher_foreground
                            EndToStart -> R.drawable.ic_delete
                            else -> null
                        }
                        icon?.let {
                            Icon(
                                painter = painterResource(it),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            ) {
                ListItem(
                    headlineContent = { Text(
                        transaction.description,
                        color = if (transaction.isRead) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        fontWeight = if (transaction.isRead) FontWeight.Light else FontWeight.ExtraBold
                    )},
                    supportingContent = { Text(
                        dateUtils.formatTimestamp(transaction.timestamp),
                        color = if (transaction.isRead) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        fontWeight = if (transaction.isRead) FontWeight.ExtraLight else FontWeight.Medium,
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
    }

    @Composable
    fun TxHistory(
        txsByDate: Map<String, List<Transaction>>,
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState
    ){
        LazyColumn (
            modifier = Modifier
        ) {
            txsByDate.forEach { (dateHeader, transactions) ->
                stickyHeader (key = dateHeader) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = dateHeader,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }// end stickyHeader

                items(
                    items = transactions,
                    key = { it.id }
                ){ transaction ->
                    TransactionUI(
                        transaction,
                        scope,
                        snackbarHostState,
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp)
                    )
                    if (transaction != transactions.last() ){
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } // end items

            }// end forEach
        }
    }

    @Composable
    fun MainCanvas(
        txsByDate: Map<String, List<Transaction>>,
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState
    ){
        TxHistory(txsByDate, scope, snackbarHostState)
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview(){
        val dummyGrouped = mapOf(
            "Hoy" to listOf(
                Transaction(0, "uBike", 10.0, 1234567890, isRead = false),
                Transaction(1, "Oxxo", 55.5, 1234567891, isRead = false)
            ),
            "Ayer" to listOf(
                Transaction(2, "MRT", 20.0, 1234567890, isRead = true)
            ),
            "25 de Diciembre" to listOf(
                Transaction(3, "Señora pancakes", 50.0, 1234567890, isRead = false),
                Transaction(4, "Cena Navidad", 500.0, 1234567890, isRead = true)
            )
        )

        FinTrackTheme() {
            MainCanvas(dummyGrouped, rememberCoroutineScope(), remember { SnackbarHostState() } )
        }
    }

}

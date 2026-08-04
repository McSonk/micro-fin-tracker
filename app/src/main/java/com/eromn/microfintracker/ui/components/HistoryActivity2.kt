package com.eromn.microfintracker.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.ui.theme.FinTrackTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.eromn.microfintracker.R
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.navigationBars

@PreviewLightDark
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    val mockTransactions = listOf(
        Transaction(1, "Bike rental", 500.0, 1722585120000, categoryId = 1),
        Transaction(2, "test", 450.0, 1722498960000, categoryId = 2),
        Transaction(3, "test", 450.0, 1721044560000, categoryId = 3)
    )

    val groupedTransactions = mapOf(
        "TODAY" to listOf(mockTransactions[0]),
        "YESTERDAY" to listOf(mockTransactions[1]),
        "WEDNESDAY, JUL 15" to listOf(mockTransactions[2])
    )

    DashboardScreen(
        username = "test",
        monthlySpent = 950.0,
        todaySpent = 500.0,
        transactionsByDate = groupedTransactions,
        onLogout = { },
        onSaveTransaction = { _, _, _ -> },
        onTransactionClick = { },
        {},
        {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    username: String,
    monthlySpent: Double,
    todaySpent: Double,
    transactionsByDate: Map<String, List<Transaction>>,
    onLogout: () -> Unit,
    onSaveTransaction: (String, Double, Long) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onUndoDelete: (Transaction) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }

    FinTrackTheme {
        val darkTheme = isSystemInDarkTheme()
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                // If darkTheme is true, inverseSurface is light -> we need dark icons (true)
                // If darkTheme is false, inverseSurface is dark -> we need light icons (false)
                WindowCompat.getInsetsController(window, view)
                    .isAppearanceLightStatusBars = darkTheme
            }
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.navigationBars,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_24),
                        contentDescription = "Add transaction",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        ) { innerPadding ->
            // BOTTOM ZONE COLOR: plain background (white in light, near-black in dark)
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // TOP ZONE: contrasting panel, scrolls away with the content
                    item {
                        UpperHeader(
                            username,
                            onLogout,
                            monthlySpent,
                            todaySpent
                        )
                    }

                    // Category Chips (bottom zone, line sits just above them)
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Transactions Section
                    item {
                        Text(
                            text = "Transactions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Grouped Transactions
                    transactionsByDate.forEach { (date, transactions) ->
                        item {
                            Text(
                                text = date,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 8.dp)
                            )
                        }

                        items(items = transactions, key = {it.id} ) { transaction ->
                            SwipeableTransactionItem(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction) },
                                onDeleteRequested = { tx ->
                                    onDeleteTransaction(tx)

                                    coroutineScope.launch{
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Deleted: ${tx.description}",
                                            actionLabel = "UNDO",
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            // C. User tapped UNDO
                                            onUndoDelete(tx)
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Bottom spacer
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }// end

        if (showAddSheet) {
            AddTransactionSheet(
                onDismiss = { showAddSheet = false },
                onSave = { desc, amount, timestamp ->
                    onSaveTransaction(desc, amount, timestamp) // Pass data up to Activity
                    showAddSheet = false // Close the sheet
                }
            )
        }
    }
}

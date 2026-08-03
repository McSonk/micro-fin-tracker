package com.eromn.microfintracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import androidx.compose.foundation.background
import com.eromn.microfintracker.utils.DateUtils

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
        onAddTransaction = { },
        onTransactionClick = { }
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
    onAddTransaction: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    FinTrackTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTransaction,
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.inverseSurface)
                                .padding(bottom = 16.dp)
                        ) {
                            UpperHeader(
                                username,
                                onLogout,
                                monthlySpent,
                                todaySpent
                            )
                        }
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

                        items(transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction) }
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
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatTime(transaction.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$${String.format("%.0f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

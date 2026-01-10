package com.eromn.microfintracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.ui.theme.FinTrackTheme

@Preview(showBackground = true, showSystemUi = true)
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

    HistoryMainCanvas(
        dummyGrouped,
        snackbarHostState = SnackbarHostState(),
        {},
        {},
        { _, _, _ -> }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryMainCanvas(
    txsByDate: Map<String, List<Transaction>>,
    snackbarHostState: SnackbarHostState,
    onToggleRead: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onSave: (String, Double, Long) -> Unit,
){
    // for the modal state
    var showSheet by remember { mutableStateOf(false) }
    // for FAB button hide
    val listState = rememberLazyListState()
    val isFabVisible by remember {
        derivedStateOf {
            !listState.isScrollInProgress || listState.firstVisibleItemIndex == 0
        }
    }
    FinTrackTheme() {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    title = {
                        Text("Histórico de transacciones")
                    }
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isFabVisible,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = {
                            showSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_input_add),
                            contentDescription = "Agregar transacción"
                        )
                    }
                }

            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
                ,
                color = MaterialTheme.colorScheme.background
            ) {
                TxHistory(
                    txsByDate,
                    onToggleRead = onToggleRead,
                    onDelete = onDelete,
                    listState
                )

                if (showSheet) {
                    AddTransactionSheet(
                        onDismiss = { showSheet = false },
                        onSave = { desc, amount, timestamp ->
                            onSave(desc, amount, timestamp)
                            showSheet = false
                        }
                    )
                }
            }// end Surface
        }// end Saffold

    }// end fintracktheme
}

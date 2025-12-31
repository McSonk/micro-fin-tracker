package com.eromn.microfintracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            // for FAB button hide
            val listState = rememberLazyListState()
            val isFabVisible by remember {
                derivedStateOf {
                    !listState.isScrollInProgress || listState.firstVisibleItemIndex == 0
                }
            }

            // for the modal state
            var showSheet by remember { mutableStateOf(false) }

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            FinTrackTheme() {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                            .padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainCanvas(
                            transactions,
                            scope,
                            snackbarHostState,
                            listState
                        )

                        if (showSheet) {
                            AddTransactionSheet(
                                onDismiss = { showSheet = false },
                                onSave = { desc, amount, timestamp ->
                                    historyViewModel.logTransaction(desc, amount, timestamp)
                                    showSheet = false
                                }
                            )
                        }
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
        snackbarHostState: SnackbarHostState,
        listState: LazyListState
    ){
        LazyColumn (
            state = listState,
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
        snackbarHostState: SnackbarHostState,
        listState: LazyListState
    ){
        TxHistory(txsByDate, scope, snackbarHostState, listState)
    }

    // Externalise component so we can preview it
    @Composable
    fun AddtransactionForm(
        dateTextValue: String,
        onDismiss: () -> Unit,
        launchDatePicker: () -> Unit,
        datePickerState: DatePickerState,
        onSave: (String, Double, Long) -> Unit
    ) {
        var description by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp) // Extra space at bottom
        ) {
            Text(
                text = "Agregar Gasto",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Monto") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { launchDatePicker() }
            ) {
                OutlinedTextField(
                    value = dateTextValue,
                    onValueChange = {},
                    label = { Text("Fecha") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false, // Prevents keyboard focus
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                            contentDescription = null
                        )
                    },
                    // We override the colors so it doesn't look "greyed out"
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val amountDouble = amount.toDoubleOrNull() ?: 0.0
                        val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        if (description.isNotBlank() && amountDouble > 0) {
                            onSave(description, amountDouble, selectedMillis)
                        }
                    }
                ) {
                    Text("Agregar")
                }
            }
        }// end Column
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddTransactionSheet(
        onDismiss: () -> Unit,
        onSave: (String, Double, Long) -> Unit
    ) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        // date stuff
        var showDatePicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        val selectedDateText = datePickerState.selectedDateMillis?.let {
            dateUtils.formatTimestamp(it)
        } ?: "Seleccionar fecha"

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets.ime }
        ) {

            AddtransactionForm(
                dateTextValue = selectedDateText,
                onDismiss = onDismiss,
                launchDatePicker = { showDatePicker = true },
                datePickerState = datePickerState,
                onSave
            )
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(onClick = { showDatePicker = false }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        } // end ModalBottomSheet
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
            MainCanvas(
                dummyGrouped,
                rememberCoroutineScope(),
                remember { SnackbarHostState() },
                rememberLazyListState()
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun AddTransactionPreview(){
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        val selectedDateText = datePickerState.selectedDateMillis?.let {
            dateUtils.formatTimestamp(it)
        } ?: "Seleccionar fecha"

        AddtransactionForm(
            dateTextValue = selectedDateText,
            onDismiss = {},
            launchDatePicker = {  },
            datePickerState = datePickerState,
            { _, _, _ -> }
        )
    }

    
}

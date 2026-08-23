package com.eromn.microfintracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.data.Category
import com.eromn.microfintracker.ui.theme.FinTrackTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.navigationBars
import com.eromn.microfintracker.domain.model.DeleteReadResult
import com.eromn.microfintracker.domain.model.UploadResult
import com.eromn.microfintracker.ui.components.AddTransactionSheet
import com.eromn.microfintracker.ui.components.SwipeableTransactionItem
import com.eromn.microfintracker.ui.components.UpperHeader

/**
 * Preview of [DashboardScreen] with sample data.
 */
@PreviewLightDark
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    val mockTransactions = listOf(
        Transaction(
            1,
            "Bike rental",
            500.0,
            1722585120000,
            categoryId = 1,
            isRead = false),
        Transaction(
            2,
            "test",
            450.0,
            1722498960000,
            categoryId = 2,
            isRead = true
        ),
        Transaction(3, "test", 450.0, 1721044560000, categoryId = 3)
    )

    val groupedTransactions = mapOf(
        "TODAY" to listOf(mockTransactions[0]),
        "YESTERDAY" to listOf(mockTransactions[1]),
        "WEDNESDAY, JUL 15" to listOf(mockTransactions[2])
    )
    
    FinTrackTheme {
        DashboardScreen(
            username = "test",
            monthlySpent = 950.0,
            todaySpent = 500.0,
            transactionsByDate = groupedTransactions,
            editingTransaction = null,
            onUploadToServer = { },
            isUploading = false,
            uploadResult = null,
            onUploadResultShown = { },
            onDeleteReadConfirmed = { },
            isDeletingRead = false,
            deleteReadResult = null,
            onDeleteReadResultShown = { },
            onSaveTransaction = { _ -> },
            onTransactionClick = { },
            onDeleteTransaction = { },
            onUndoDelete = { },
            onDismissTransactionSheet = { },
            selectedCategory = Category.OTHERS,
            isCategoryPickerVisible = false,
            categorySearchQuery = "",
            filteredCategories = emptyList(),
            onAddTransactionRequested = { },
            onCategoryFieldClicked = { },
            onCategorySearchQueryChanged = { },
            onCategorySelected = { },
            onDismissCategoryPicker = { }
        )
    }
}

/**
 * Main dashboard screen for the history feature.
 *
 * @param username name of the logged-in user, shown in the header.
 * @param monthlySpent total monthly spending shown in the header.
 * @param todaySpent total spending today shown in the header.
 * @param transactionsByDate transactions grouped by a header label (e.g. "Hoy", "Ayer").
 * @param editingTransaction transaction currently being edited, if any.
 * @param onUploadToServer callback when the user requests to upload pending transactions.
 * @param isUploading true while an upload is in progress; blocks interaction and shows a spinner.
 * @param uploadResult outcome of the last upload attempt, if any, to surface via snackbar.
 * @param onUploadResultShown callback invoked after the upload result snackbar is shown.
 * @param onDeleteReadConfirmed callback invoked when the user confirms deleting all read transactions.
 * @param isDeletingRead true while read transactions are being deleted; blocks interaction and shows a spinner.
 * @param deleteReadResult outcome of the last delete-read-transactions attempt, if any, to surface via snackbar.
 * @param onDeleteReadResultShown callback invoked after the delete result snackbar is shown.
 * @param onSaveTransaction callback invoked with a transaction to save (create or update).
 * @param onTransactionClick callback invoked when a transaction item is clicked, typically to edit it.
 * @param onDeleteTransaction callback invoked when a transaction should be deleted.
 * @param onUndoDelete callback invoked when the user taps UNDO after a delete snackbar.
 * @param onDismissTransactionSheet callback invoked when the transaction edit/add sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    username: String,
    monthlySpent: Double,
    todaySpent: Double,
    transactionsByDate: Map<String, List<Transaction>>,
    editingTransaction: Transaction?,
    onUploadToServer: () -> Unit,
    isUploading: Boolean,
    uploadResult: UploadResult?,
    onUploadResultShown: () -> Unit,
    onDeleteReadConfirmed: () -> Unit,
    isDeletingRead: Boolean,
    deleteReadResult: DeleteReadResult?,
    onDeleteReadResultShown: () -> Unit,
    onSaveTransaction: (Transaction) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onUndoDelete: (Transaction) -> Unit,
    onDismissTransactionSheet: () -> Unit,
    selectedCategory: Category,
    isCategoryPickerVisible: Boolean,
    categorySearchQuery: String,
    filteredCategories: List<Category>,
    onAddTransactionRequested: () -> Unit,
    onCategoryFieldClicked: () -> Unit,
    onCategorySearchQueryChanged: (String) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onDismissCategoryPicker: () -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDeleteSuccess by remember { mutableStateOf(false) }

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
        val context = LocalContext.current

        LaunchedEffect(uploadResult) {
            if (uploadResult != null) {
                val message = when (uploadResult) {
                    is UploadResult.NoPendingTransactions ->
                        context.getString(R.string.snackbar_no_pending_transactions)
                    is UploadResult.Summary ->
                        context.getString(
                            R.string.snackbar_upload_summary_format,
                            uploadResult.successCount,
                            uploadResult.errorCount
                        )
                }
                snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
                onUploadResultShown()
            }
        }

        LaunchedEffect(deleteReadResult) {
            if (deleteReadResult != null) {
                when (deleteReadResult) {
                    is DeleteReadResult.NoReadTransactions ->
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.snackbar_no_read_transactions),
                            duration = SnackbarDuration.Short
                        )
                    is DeleteReadResult.Deleted ->
                        // Success is communicated with the animated checkmark overlay alone.
                        showDeleteSuccess = true
                    is DeleteReadResult.Failed ->
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.snackbar_delete_read_error),
                            duration = SnackbarDuration.Short
                        )
                }
                onDeleteReadResultShown()
            }
        }

        LaunchedEffect(showDeleteSuccess) {
            if (showDeleteSuccess) {
                delay(1500)
                showDeleteSuccess = false
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.navigationBars,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (!isUploading && !isDeletingRead) {
                            onAddTransactionRequested()
                            showAddSheet = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_24),
                        contentDescription = stringResource(R.string.add_tx_title),
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
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // TOP ZONE: contrasting panel, scrolls away with the content
                    item {
                        UpperHeader(
                            username,
onUploadToServer,
                            isUploading,
                            { showDeleteConfirm = true },
                            isDeletingRead,
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
                            text = stringResource(R.string.dashboard_transactions_title),
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

                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = context.getString(
                                                R.string.snackbar_deleted_format,
                                                tx.description
                                            ),
                                            actionLabel = context.getString(R.string.snackbar_undo),
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

                    if (isUploading || isDeletingRead) {
                        // Blocks all pointer input and dims the list while an operation is in progress.
                        val progressDescription =
                            if (isDeletingRead) R.string.deleting_read_content_description
                            else R.string.uploading_content_description
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) awaitPointerEvent()
                                    }
                                }
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .semantics {
                                        contentDescription = context.getString(progressDescription)
                                    }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showDeleteSuccess,
                        enter = fadeIn(initialAlpha = 0.0f) + scaleIn(initialScale = 0.6f),
                        exit = fadeOut()
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(112.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = context.getString(
                                        R.string.delete_read_success_content_description
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(64.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
            }
        }
    }

        if (showDeleteConfirm && !isDeletingRead && !isUploading) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(stringResource(R.string.delete_read_confirm_title)) },
                text = { Text(stringResource(R.string.delete_read_confirm_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        onDeleteReadConfirmed()
                    }) {
                        Text(stringResource(R.string.common_accept))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }

        if (showAddSheet || editingTransaction != null) {
            AddTransactionSheet(
                initialTransaction = editingTransaction,
                selectedCategory = selectedCategory,
                isCategoryPickerVisible = isCategoryPickerVisible,
                categorySearchQuery = categorySearchQuery,
                filteredCategories = filteredCategories,
                onCategoryFieldClicked = onCategoryFieldClicked,
                onCategorySearchQueryChanged = onCategorySearchQueryChanged,
                onCategorySelected = onCategorySelected,
                onDismissCategoryPicker = onDismissCategoryPicker,
                onDismiss = {
                    showAddSheet = false
                    onDismissTransactionSheet()
                },
                onSave = { transaction ->
                    showAddSheet = false
                    onSaveTransaction(transaction)
                }
            )
        }
    }
}

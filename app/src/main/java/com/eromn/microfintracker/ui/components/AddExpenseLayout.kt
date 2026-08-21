package com.eromn.microfintracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eromn.microfintracker.R
import com.eromn.microfintracker.data.Category
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.utils.DateUtils
import java.util.Calendar

/**
 * Preview for [AddTransactionForm].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AddTransactionPreview(){
    // date stuff
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.getTodayUtcMidnight()
    )

    // Time stuff
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    AddTransactionForm(
        onDismiss = {},
        launchDatePicker = {  },
        launchTimePicker = { },
        datePickerState = datePickerState,
        timePickerState = timePickerState,
        initialTransaction = null,
        selectedCategory = Category.OTHERS,
        onCategoryFieldClicked = { },
        onSave = { _ -> }
    )
}// end AddTransactionPreview

/**
 * Modal bottom sheet that wraps [AddTransactionForm] and provides date/time picker dialogs.
 *
 * @param initialTransaction optional transaction to edit; when null a new transaction will be created.
 * @param onDismiss callback invoked when the sheet is dismissed.
 * @param onSave callback invoked with the transaction to be saved (new or updated).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    initialTransaction: Transaction? = null,
    selectedCategory: Category,
    isCategoryPickerVisible: Boolean,
    categorySearchQuery: String,
    filteredCategories: List<Category>,
    onCategoryFieldClicked: () -> Unit,
    onCategorySearchQueryChanged: (String) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onDismissCategoryPicker: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    // date stuff
    var showDatePicker by remember { mutableStateOf(false) }
    val initialTimestamp = initialTransaction?.timestamp
    val currentTime = Calendar.getInstance()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (initialTimestamp != null) {
            DateUtils.getUtcMidnightForLocalDate(initialTimestamp)
        } else {
            DateUtils.getTodayUtcMidnight()
        }
    )

    // Time stuff
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = if (initialTimestamp != null) {
            DateUtils.getLocalHour(initialTimestamp)
        } else {
            currentTime.get(Calendar.HOUR_OF_DAY)
        },
        initialMinute = if (initialTimestamp != null) {
            DateUtils.getLocalMinute(initialTimestamp)
        } else {
            currentTime.get(Calendar.MINUTE)
        },
        is24Hour = false,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.ime }
    ) {

        AddTransactionForm(
            initialTransaction = initialTransaction,
            selectedCategory = selectedCategory,
            onCategoryFieldClicked = onCategoryFieldClicked,
            launchDatePicker = { showDatePicker = true },
            launchTimePicker = { showTimePicker = true },
            datePickerState = datePickerState,
            timePickerState = timePickerState,
            onDismiss = onDismiss,
            onSave = onSave
        )
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.common_accept))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }// end date modal
        if (showTimePicker) { // time modal
            TimePickerDialog(
                title = { Text("Selecciona la hora") },
                onDismissRequest = { showTimePicker = false  },
                confirmButton = {
                    Button(onClick = { showTimePicker = false }) {
                        Text(stringResource(R.string.common_accept))
                    }
                }
            ) {
                TimePicker(state = timePickerState)
            }// end timePickerDialog
        }// end time modal
    } // end ModalBottomSheet

    if (isCategoryPickerVisible) {
        CategoryPickerDialog(
            categories = filteredCategories,
            selectedCategoryId = selectedCategory.serverId,
            searchQuery = categorySearchQuery,
            onSearchQueryChange = onCategorySearchQueryChanged,
            onCategorySelected = onCategorySelected,
            onDismiss = onDismissCategoryPicker
        )
    }
}// end AddTransactionSheet

/**
 * Composable form for adding or editing a transaction.
 *
 * @param datePickerState state of the date picker used to select the transaction date.
 * @param timePickerState state of the time picker used to select the transaction time.
 * @param launchDatePicker callback that opens the date picker dialog.
 * @param launchTimePicker callback that opens the time picker dialog.
 * @param initialTransaction optional transaction being edited; when null a new transaction is entered.
 * @param onDismiss callback invoked when the user cancels the form.
 * @param onSave callback invoked with the constructed transaction when the user confirms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionForm(
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    launchDatePicker: () -> Unit,
    launchTimePicker: () -> Unit,
    initialTransaction: Transaction? = null,
    selectedCategory: Category,
    onCategoryFieldClicked: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
) {
    var YOUBIKE_TXT = "YouBike"
    var MRT_TXT = "MRT"

    var description by remember(initialTransaction?.id) {
        mutableStateOf(initialTransaction?.description.orEmpty())
    }
    var amount by remember(initialTransaction?.id) {
        mutableStateOf(initialTransaction?.amount?.toString().orEmpty())
    }
    val selectedDateText = DateUtils.formatDateUTC(datePickerState.selectedDateMillis!!)
    val selectedTimeText = DateUtils.formatTime(timePickerState.hour, timePickerState.minute)

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .padding(bottom = 32.dp) // Extra space at bottom
    ) {
        Text(
            text = stringResource(
                if (initialTransaction == null) {
                    R.string.add_tx_title
                } else {
                    R.string.edit_tx_title
                }
            ),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Spacer(modifier = Modifier.width(6.dp))
            Button(onClick = {
                if (description == YOUBIKE_TXT){
                    val originalAmount = amount.toDoubleOrNull() ?: 0.0
                    amount = (originalAmount + 10.0).toString()
                }
                else {
                    description = YOUBIKE_TXT
                    amount = 10.0.toString()
                }
            }) {
                Text("YouBike")
            }

            Button(onClick = {
                if (description == MRT_TXT) {
                    val originalAmount = amount.toDoubleOrNull() ?: 0.0
                    amount = (originalAmount + 5.0).toString()
                } else {
                    description = MRT_TXT
                    amount = 20.0.toString()
                }
            }) {
                Text("MRT")
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.add_tx_description)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(R.string.add_tx_amount)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCategoryFieldClicked() }
        ) {
            OutlinedTextField(
                value = selectedCategory.displayName,
                onValueChange = {},
                label = { Text(stringResource(R.string.select_category_title)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_drop_down_24),
                        contentDescription = null
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(// Date and time
            modifier = Modifier.fillMaxWidth()
        ){
            Box(// Date
                modifier = Modifier
                    .weight(1f)
                    .clickable { launchDatePicker() }
            ) {
                OutlinedTextField(
                    value = selectedDateText,
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
                )// end outlinedTextField
            }// end box
            Spacer(modifier = Modifier.width(8.dp))
            Box(// Time
                modifier = Modifier
                    .weight(1f)
                    .clickable{ launchTimePicker() }
            ){
                OutlinedTextField(
                    value = selectedTimeText,
                    onValueChange = {},
                    label = { Text("Hora") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_dialog_map),
                            contentDescription = "Un reloj",
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }// end row (date and time)

        Row( //Buttons
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            val amountDouble = amount.toDoubleOrNull() ?: 0.0
            val isValid = description.isNotBlank() && amountDouble.isFinite() && amountDouble > 0
            Button(
                enabled = isValid,
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    if (selectedDate != null) {
                        val selectedMillis = DateUtils.mergeDateTimeUTC(
                            selectedDate,
                            timePickerState.hour,
                            timePickerState.minute
                        )

                        val transactionToSave = initialTransaction?.copy(
                            description = description.trim(),
                            amount = amountDouble,
                            timestamp = selectedMillis,
                            categoryId = selectedCategory.serverId
                        ) ?: Transaction(
                            description = description.trim(),
                            amount = amountDouble,
                            timestamp = selectedMillis,
                            categoryId = selectedCategory.serverId
                        )

                        onSave(transactionToSave)
                    }
                }
            ) {
                Text(
                    stringResource(
                        if (initialTransaction == null) {
                            R.string.add_tx_btn_add
                        } else {
                            R.string.common_accept
                        }
                    )
                )
            }
        } // end buttons row
    }// end Column
}//end TransactionForm

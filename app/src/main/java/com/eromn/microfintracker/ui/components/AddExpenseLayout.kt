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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eromn.microfintracker.utils.DateUtils
import java.util.Calendar

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
        onSave = { _, _, _ -> }
    )
}// end AddTransactionPreview

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
        initialSelectedDateMillis = DateUtils.getTodayUtcMidnight()
    )


    // Time stuff
    var showTimePicker by remember { mutableStateOf(false) }
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.ime }
    ) {

        AddTransactionForm(
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
                        Text("Aceptar")
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
                        Text("Aceptar")
                    }
                }
            ) {
                TimePicker(state = timePickerState)
            }// end timePickerDialog
        }// end time modal
    } // end ModalBottomSheet
}// end AddTransactionSheet

// Externalise component so we can preview it
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionForm(
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    launchDatePicker: () -> Unit,
    launchTimePicker: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Double, Long) -> Unit,
) {
    var YOUBIKE_TXT = "YouBike"
    var MRT_TXT = "MRT"

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val selectedDateText = DateUtils.formatDateUTC(datePickerState.selectedDateMillis!!)
    val selectedTimeText = DateUtils.formatTime(timePickerState.hour, timePickerState.minute)

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
                Text("Cancelar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    val selectedDate = datePickerState.selectedDateMillis
                    val selectedMillis = DateUtils.mergeDateTimeUTC(selectedDate!!, timePickerState.hour, timePickerState.minute)
                    if (description.isNotBlank() && amountDouble > 0) {
                        onSave(description, amountDouble, selectedMillis)
                    }
                }
            ) {
                Text("Agregar")
            }
        } // end buttons row
    }// end Column
}//end TransactionForm

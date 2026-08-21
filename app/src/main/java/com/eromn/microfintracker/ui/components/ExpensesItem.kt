package com.eromn.microfintracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eromn.microfintracker.R
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.utils.DateUtils
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.eromn.microfintracker.ui.theme.FinTrackTheme
import java.util.Calendar
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.eromn.microfintracker.data.Category
import com.eromn.microfintracker.ui.extensions.iconRes

/**
 * Preview for [TransactionItem] with a sample transaction.
 */
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun PreviewItem(){
    val currentTime = Calendar.getInstance()
    val tx = Transaction(
        1,
        "Bike rental",
        500.0,
        currentTime.timeInMillis,
        categoryId = 1)


    FinTrackTheme{
        TransactionItem(
            tx,
            {}
        )
    }
}

/**
 * Displays a single transaction in a card.
 *
 * @param transaction The transaction to display.
 * @param onClick Callback invoked when the card is clicked.
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    // Safely resolve the category. If null, your enum's fromId() defaults to OTHERS.
    val category = Category.fromId(transaction.categoryId)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = category.iconRes), // Clean mapping!
                    contentDescription = category.displayName,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${category.displayName} • ${DateUtils.formatTime(transaction.timestamp)}",
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

/**
 * Displays a transaction item with swipe-to-delete functionality.
 *
 * @param transaction The transaction to display.
 * @param onClick Callback invoked when the item is clicked.
 * @param onDeleteRequested Callback invoked when the user swipes to delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onDeleteRequested: (Transaction) -> Unit
) {
    // Flag to ensure the side-effect only fires once per swipe,
    // even if the swipe machinery consults confirmValueChange multiple times during the settle animation.
    // By passing transaction.id, we tell Compose: "If the ID changes, throw away the
    // old state and create a fresh one." This prevents hasTriggered from staying 'true'
    // if the UI is recycled for a different transaction.
    var hasTriggered by remember(transaction.id) { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { newValue ->
            if (newValue == SwipeToDismissBoxValue.EndToStart) {
                if (!hasTriggered) {
                    hasTriggered = true
                    onDeleteRequested(transaction)
                }
                // Return false to reject the state change.
                // This automatically triggers the native spring-back animation!
                false
            } else {
                true
            }
        }
    )

    // If this transaction is "Undo"ed and re-added to the list, or if Compose recycles
    // this row for a newly added transaction, this guarantees the swipe offset is
    // visually reset to 0, preventing the "idle/stuck" bug.
    LaunchedEffect(transaction.id) {
        dismissState.reset()
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false, // right swipe reserved for future "Edit"
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    ) {
        TransactionItem(transaction = transaction, onClick = onClick)
    }
}

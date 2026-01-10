package com.eromn.microfintracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eromn.microfintracker.R
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.utils.DateUtils
import kotlin.math.abs

@Composable
fun TxHistory(
    txsByDate: Map<String, List<Transaction>>,
    onToggleRead: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
){
    LazyColumn (
        state = listState,
        modifier = modifier
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
                    onToggleRead = onToggleRead,
                    onDelete = onDelete,
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
}// end txHistory

@Composable
fun TransactionUI(
    transaction: Transaction,
    onToggleRead: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
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
                onToggleRead(transaction)
                // Reset state so it snaps back
                swipeToDismissBoxState.snapTo(Settled)
            }
            EndToStart -> {
                onDelete(transaction)
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
                    DateUtils.formatTime(transaction.timestamp),
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
}// end TransactionUI
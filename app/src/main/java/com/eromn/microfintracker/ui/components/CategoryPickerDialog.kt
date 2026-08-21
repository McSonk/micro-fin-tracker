package com.eromn.microfintracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.eromn.microfintracker.R
import com.eromn.microfintracker.data.Category
import com.eromn.microfintracker.ui.extensions.iconRes
import com.eromn.microfintracker.ui.theme.FinTrackTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun CategoryPickerDialogPreview() {
    FinTrackTheme {
        CategoryPickerDialog(
            categories = listOf(
                Category.TRANSPORT,
                Category.FOOD,
                Category.ENTERTAINMENT,
                Category.HEALTH,
                Category.OTHERS
            ),
            selectedCategoryId = Category.FOOD.serverId, // Simulates "Food" being selected
            searchQuery = "",
            onSearchQueryChange = {},
            onCategorySelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun CategoryPickerDialogEmptySearchPreview() {
    FinTrackTheme {
        CategoryPickerDialog(
            categories = emptyList(), // Simulates no search results
            selectedCategoryId = Category.OTHERS.serverId,
            searchQuery = "xyz", // Simulates an active search query
            onSearchQueryChange = {},
            onCategorySelected = {},
            onDismiss = {}
        )
    }
}

@Composable
fun CategoryPickerDialog(
    categories: List<Category>,
    selectedCategoryId: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(500.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .imePadding()
            ) {
                Text(
                    text = stringResource(R.string.select_category_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text(stringResource(R.string.category_search_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (categories.isEmpty()) {
                    Text(
                        text = stringResource(R.string.category_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(categories, key = { it.serverId }) { category ->
                            val isSelected = category.serverId == selectedCategoryId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCategorySelected(category) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Icon Badge
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = category.iconRes),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    if (category.description.isNotBlank()) {
                                        Text(
                                            text = category.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = stringResource(R.string.category_selected_content_description),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

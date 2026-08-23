package com.eromn.microfintracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eromn.microfintracker.R
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars

@Preview
@Composable
fun PreviewHeaderLayout() {
    UpperHeader(
        "McSonk",
        {},
        500.0,
        100.0
    )
}

@Composable
fun UpperHeader(
    username: String,
    onUploadToServer: () -> Unit,
    monthlySpent: Double,
    todaySpent: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars) // reserve status bar space first
            .background(MaterialTheme.colorScheme.inverseSurface) // then paint over it
            .padding(bottom = 16.dp)
    ) {
        HeaderSection(username, onUploadToServer)
        Spacer(modifier = Modifier.height(24.dp))
        SpendingSummary(monthlySpent, todaySpent)
    }
}

@Composable
private fun HeaderSection(
    username: String,
    onUploadToServer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.header_welcome_back),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = username,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }

            IconButton(onClick = onUploadToServer) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout_24),
                    contentDescription = stringResource(R.string.upload_content_description),
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}

@Composable
private fun SpendingSummary(
    monthlySpent: Double,
    todaySpent: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.header_spent_this_month),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$${String.format("%.0f", monthlySpent)}",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.inverseOnSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.header_today_spent, String.format("%.0f", todaySpent)),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
        )
    }
}

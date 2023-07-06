package com.github.alunegov.ble2.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.alunegov.ble2.android.ui.theme.Ble2Theme

@Composable
fun SelectDeviceScreen(
    uiState: DevicesModel,
    onDeviceClick: (id: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(uiState.devices) {device ->
            Column(
                modifier = Modifier
                    .clickable { onDeviceClick(device.mac) }
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text(
                    text = device.name ?: stringResource(R.string.unknown_server_name),
                    style = MaterialTheme.typography.headlineSmall,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = device.mac,
                        modifier = Modifier.weight(1.0f),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Text(
                        text = stringResource(R.string.server_rssi, device.rssi),
                        modifier = Modifier.weight(1.0f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Preview(locale = "ru", showBackground = true)
@Composable
fun SelectDeviceScreenPreview() {
    Ble2Theme {
        SelectDeviceScreen(
            DevicesModel(
                devices = listOf(
                    Device("10:20:30:40:50:61", "Server 1", -10),
                    Device("10:20:30:40:50:62", "Server 2", -20),
                ),
                errorText = "Error",
            ),
            {},
        )
    }
}

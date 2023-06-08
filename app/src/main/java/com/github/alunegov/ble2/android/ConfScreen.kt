package com.github.alunegov.ble2.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.alunegov.ble2.android.ui.theme.Ble2Theme

@Composable
fun ConfScreen(
    uiState: ConfUiState,
    onSaveClick: (kp: Float, ki: Float, kd: Float) -> Unit,
) {
    val kp = remember(uiState.kp) { mutableStateOf(uiState.kp.toString()) }
    val ki = remember(uiState.ki) { mutableStateOf(uiState.ki.toString()) }
    val kd = remember(uiState.kd) { mutableStateOf(uiState.kd.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("kp", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = kp.value,
                onValueChange = { kp.value = it },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("ki", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = ki.value,
                onValueChange = { ki.value = it },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("kd", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = kd.value,
                onValueChange = { kd.value = it },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
            )
        }

        Button(
            onClick = {
                val kpv = kp.value.toFloat()
                val kiv = ki.value.toFloat()
                val kdv = kd.value.toFloat()
                onSaveClick(kpv, kiv, kdv)
            },
        ) {
            Text("Сохранить")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfScreenPreview() {
    Ble2Theme {
        ConfScreen(
            ConfUiState(1.0f, 0.0f, 0.0f, errorText = "Error"),
            { _, _, _ -> Unit },
        )
    }
}

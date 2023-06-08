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
import androidx.compose.material3.MaterialTheme
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
fun Form1Screen(
    uiState: Form1UiState,
    onCalcStartCurrentClick: (i1: Float, i2: Float) -> Unit,
    onSetConfClick: () -> Unit,
    onFurtherClick: (startCurrent: Float) -> Unit,
) {
    val i1 = remember(uiState.i1) { mutableStateOf(uiState.i1.toString()) }
    val i2 = remember(uiState.i2) { mutableStateOf(uiState.i2.toString()) }
    val i3 = remember(uiState.startCurrent) { mutableStateOf(uiState.startCurrent.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Bluetooth связь")

            Spacer(Modifier.size(16.dp))

            Text(uiState.name ?: "nope", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            Button(
                onClick = onSetConfClick,
                //enabled = uiState.connected,
            ) {
                Text("*")
            }
        }

        Text("Введите данные:")

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("I (ВН) ном., А", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = i1.value,
                onValueChange = { i1.value = it },
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
            Text("Ixx (ВН), %", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = i2.value,
                onValueChange = { i2.value = it },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
            )
        }

        Button(
            onClick = {
                val i1v = i1.value.toFloat()
                val i2v = i2.value.toFloat()
                onCalcStartCurrentClick(i1v, i2v)
            },
        ) {
            Text("Расчитать")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("I разм. нач., А", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = i3.value,
                onValueChange = { i3.value = it },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
            )
        }

        Button(
            onClick = {
                val i3v = i3.value.toFloat()
                onFurtherClick(i3v)
            },
            enabled = uiState.connected,
        ) {
            Text("Продолжить")
        }

        if (uiState.errorText.isNotEmpty()) {
            Text(uiState.errorText, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Form1ScreenPreview() {
    Ble2Theme {
        Form1Screen(
            Form1UiState(false, null, 381.7f, 0.45f, 0.0f, errorText = "Error"),
            { _, _ -> Unit},
            {},
            {},
        )
    }
}

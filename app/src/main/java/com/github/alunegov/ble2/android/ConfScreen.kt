package com.github.alunegov.ble2.android

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.alunegov.ble2.android.ui.theme.Ble2Theme

private const val TAG = "ConfScreen"

@Composable
fun ConfScreen(
    uiState: ConfUiState,
    onSaveClick: (kp: Float, ki: Float, kd: Float) -> Unit,
) {
    val scrollState = rememberScrollState()

    var kp by rememberSaveable(uiState.kp) { mutableStateOf(uiState.kp.toString()) }
    var ki by rememberSaveable(uiState.ki) { mutableStateOf(uiState.ki.toString()) }
    var kd by rememberSaveable(uiState.kd) { mutableStateOf(uiState.kd.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 0.dp)
            .verticalScroll(scrollState),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Коэффициент P", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = kp,
                onValueChange = { kp = it },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Коэффициент I", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = ki,
                onValueChange = { ki = it },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Коэффициент D", Modifier.weight(1.0f))

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = kd,
                onValueChange = { kd = it },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
            )
        }

        Button(
            onClick = {
                try {
                    val kpv = kp.toFloat()
                    val kiv = ki.toFloat()
                    val kdv = kd.toFloat()
                    onSaveClick(kpv, kiv, kdv)
                } catch (e: Exception) {
                    Log.d(TAG, e.message ?: e.toString())
                }
            },
            //modifier = Modifier.padding(0.dp, 8.dp),
        ) {
            Text("Сохранить")
        }

        if (uiState.errorText.isNotEmpty()) {
            Text(uiState.errorText, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(locale = "ru", showBackground = true)
@Composable
fun ConfScreenPreview() {
    Ble2Theme {
        ConfScreen(
            ConfUiState(1.0f, 0.0f, 0.0f, errorText = "Error"),
            { _, _, _ -> Unit },
        )
    }
}

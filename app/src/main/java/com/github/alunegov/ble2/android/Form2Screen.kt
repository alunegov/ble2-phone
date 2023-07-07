package com.github.alunegov.ble2.android

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.alunegov.ble2.android.ui.theme.Ble2Theme
import java.text.NumberFormat

@Composable
fun Form2Screen(
    uiState: Form2UiState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onShowResultsClick: () -> Unit,
    onHideResultsClick: () -> Unit,
) {
    if (!uiState.showingResults) {
        Form2MainScreen(
            uiState,
            onStartClick,
            onStopClick,
            onShowResultsClick,
        )
    } else {
        Form2ResultsScreen(
            uiState,
            onHideResultsClick,
        )
    }
}

@Composable
fun Form2MainScreen(
    uiState: Form2UiState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onShowResultsClick: () -> Unit,
) {
    val numFmt = NumberFormat.getInstance().apply {
        maximumFractionDigits = 5
    }

    val scrollState = rememberScrollState()

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
            OutlinedTextField(
                value = numFmt.format(uiState.startCurrent),
                onValueChange = {},
                modifier = Modifier.width(150.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text("Начальный ток, А")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uiState.cycleNum.toString(),
                onValueChange = {},
                modifier = Modifier.width(150.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text("Номер цикла")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = numFmt.format(uiState.currentUp),
                onValueChange = {},
                modifier = Modifier.width(150.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text("Заданный ток, А")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uiState.polarity.toString(),
                onValueChange = {},
                modifier = Modifier.width(150.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text("Полярность")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = numFmt.format(uiState.current),
                onValueChange = {},
                modifier = Modifier.width(150.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text("Измеренный ток, А")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(uiState.state, Modifier.weight(1.0f))

            Button(
                onClick = onShowResultsClick,
                enabled = uiState.resultsAvail,
            ) {
                Text("Сводка")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Button(
                onClick = onStartClick,
                enabled = uiState.startEnabled,
            ) {
                Text("Запуск")
            }

            Button(
                onClick = onStopClick,
                enabled = uiState.stopEnabled,
            ) {
                Text("Стоп")
            }
        }

        if (uiState.errorText.isNotEmpty()) {
            Text(uiState.errorText, color = MaterialTheme.colorScheme.error)
        }

        //Text(uiState.connStateText, color = MaterialTheme.colorScheme.secondary)
    }

    // prevent exit while not fully stopped
    BackHandler(uiState.connected && !uiState.startEnabled) {
        //onStopClick()
    }
}

@Preview(locale = "ru", showBackground = true)
@Composable
fun Form2ScreenMainPreview() {
    Ble2Theme {
        Form2MainScreen(
            Form2UiState(state = "Идёт размагничивание", errorText = "Error"),
            {},
            {},
            {},
        )
    }
}

@Composable
fun Form2ResultsScreen(
    uiState: Form2UiState,
    onHideResultsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Цикл", Modifier.weight(1.0f), textAlign = TextAlign.Center)

            Text("Заданный ток, А", Modifier.weight(1.0f), textAlign = TextAlign.Center)

            Text("Установленный ток, А", Modifier.weight(1.0f), textAlign = TextAlign.Center)

            Text("Время цикла, с", Modifier.weight(1.0f), textAlign = TextAlign.Center)
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(uiState.results) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(it.num.toString(), Modifier.weight(1.0f), textAlign = TextAlign.Center)

                    Text("%.3f".format(it.currentUp), Modifier.weight(1.0f), textAlign = TextAlign.Center)

                    Text("%.3f".format(it.current), Modifier.weight(1.0f), textAlign = TextAlign.Center)

                    Text(it.duration.toString(), Modifier.weight(1.0f), textAlign = TextAlign.Center)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Общее время, с:", Modifier.weight(3.0f))

                    Text(uiState.resultsDuration.toString(), Modifier.weight(1.0f), textAlign = TextAlign.Center)
                }
            }
        }
    }

    BackHandler(true) {
        onHideResultsClick()
    }
}

@Preview(locale = "ru", showBackground = true)
@Composable
fun Form2ResultsScreenPreview() {
    Ble2Theme {
        Form2ResultsScreen(
            Form2UiState(
                results = listOf(
                    CycleStat(1u, 3.435f,3.434f, 10L),
                    CycleStat(2u, 2.405f,2.404f, 20L),
                    CycleStat(3u, 1.683f,1.683f, 30L),
                ),
                resultsDuration = 100500L,
                errorText = "Error",
            ),
            {},
        )
    }
}

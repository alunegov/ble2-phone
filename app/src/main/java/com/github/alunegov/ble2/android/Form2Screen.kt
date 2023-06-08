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
            items = uiState.results,
            duration = uiState.resultsDuration,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uiState.startCurrent.toString(),
                onValueChange = {},
                modifier = Modifier.width(150.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text("Начальный ток, А")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uiState.currentUp.toString(),
                onValueChange = {},
                modifier = Modifier.width(150.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text("Заданный ток, А")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uiState.current.toString(),
                onValueChange = {},
                modifier = Modifier.width(150.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text("Измеренный ток, А")
        }

        Row(
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
            modifier = Modifier.fillMaxWidth(),
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
    }

    BackHandler(false) {
        onStopClick()
    }
}

@Preview(showBackground = true)
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
    items: List<CycleStat>,
    duration: Long,
    onHideResultsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Цикл", Modifier.weight(1.0f), textAlign = TextAlign.Center)

            Text("Заданный ток, А", Modifier.weight(1.0f), textAlign = TextAlign.Center)

            Text("Установленный ток, А", Modifier.weight(1.0f), textAlign = TextAlign.Center)

            Text("Время цикла, сек", Modifier.weight(1.0f), textAlign = TextAlign.Center)
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(items) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(it.cycle.num.toString(), Modifier.weight(1.0f), textAlign = TextAlign.Center)

                    Text("%.3f".format(it.cycle.currentUp), Modifier.weight(1.0f), textAlign = TextAlign.Center)

                    Text("%.3f".format(it.current), Modifier.weight(1.0f), textAlign = TextAlign.Center)

                    Text((it.duration / 1000).toString(), Modifier.weight(1.0f), textAlign = TextAlign.Center)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Общее время, сек:", Modifier.weight(3.0f))

                    Text((duration / 1000).toString(), Modifier.weight(1.0f), textAlign = TextAlign.Center)
                }
            }
        }
    }

    BackHandler(true) {
        onHideResultsClick()
    }
}

@Preview(showBackground = true)
@Composable
fun Form2ResultsScreenPreview() {
    Ble2Theme {
        Form2ResultsScreen(
            listOf(
                CycleStat(Cycle(1u, 3.435f, false),3.434f, 10000L),
                CycleStat(Cycle(2u, 2.405f, true),2.404f, 20000L),
                CycleStat(Cycle(3u, 1.683f, false),1.683f, 30000L),
            ),
            100500000L,
            {},
        )
    }
}

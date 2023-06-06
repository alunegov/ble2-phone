package com.github.alunegov.ble2.android

import android.os.Bundle
import android.service.autofill.OnClickAction
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.alunegov.ble2.android.ui.theme.Ble2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Ble2Theme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Root()
                }
            }
        }
    }
}

@Composable
fun Root() {
    val navController = rememberNavController();

    NavHost(navController = navController, startDestination = "A") {
        composable("A") {
            Form1({ navController.navigate("B") })
        }
        composable("B") {
            Form2({ navController.popBackStack() })
        }
    }
}

@Composable
fun Form1(onClick: () -> Unit) {
    val i1 = remember { mutableStateOf(381.7f.toString()) }
    val i2 = remember { mutableStateOf(0.45f.toString()) }
    val i3 = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp, 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Bluetooth связь",
                //modifier = Modifier.align(Alignment.CenterVertically),
            )

            //Spacer(Modifier.size(16.dp))
        }

        Text(
            text = "Введите данные:",
            //modifier = Modifier.align(Alignment.CenterVertically),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "I (ВН) ном., А",
                modifier = Modifier.align(Alignment.CenterVertically).weight(1.0f),
            )

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = i1.value,
                onValueChange = { i1.value = it },
                modifier = Modifier.width(150.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Ixx (ВН), %",
                modifier = Modifier.align(Alignment.CenterVertically).weight(1.0f),
            )

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = i2.value,
                onValueChange = { i2.value = it },
                modifier = Modifier.width(150.dp),
            )
        }

        Button(
            onClick = {
                val i1v = i1.value.toFloat()
                val i2v = i2.value.toFloat()
                i3.value = ((i2v * i1v) / 100 * 2).toString()
            },
        ) {
            Text("Расчитать")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "I разм. нач., А",
                modifier = Modifier.align(Alignment.CenterVertically).weight(1.0f),
            )

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = i3.value,
                onValueChange = { i3.value = it },
                modifier = Modifier.width(150.dp),
            )
        }

        Button(onClick = { onClick() }) {
            Text("Продолжить")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Form1Preview() {
    Ble2Theme {
        Form1({})
    }
}

@Composable
fun Form2(onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp, 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = "1.0",
                onValueChange = {},
                modifier = Modifier.width(100.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text(
                text = "Начальный ток",
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = "1.0",
                onValueChange = {},
                modifier = Modifier.width(100.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text(
                text = "Номер цикла",
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = "1.0",
                onValueChange = {},
                modifier = Modifier.width(100.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text(
                text = "Заданный ток",
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = "1.0",
                onValueChange = {},
                modifier = Modifier.width(100.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text(
                text = "Полярность",
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = "1.0",
                onValueChange = {},
                modifier = Modifier.width(100.dp),
                readOnly = true,
            )

            Spacer(Modifier.size(16.dp))

            Text(
                text = "Измеренный ток",
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }

        Text("Идёт размагничивание")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(onClick = {}) {
                Text("Запуск")
            }

            Button(onClick = { onClick() }) {
                Text("Стоп")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Form2Preview() {
    Ble2Theme {
        Form2({})
    }
}

package com.github.alunegov.ble2.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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


/**
 * Объект для работы с BT-адаптером.
 */
val gBleService = BleService()

@Composable
fun Root() {
    val navController = rememberNavController();

    NavHost(navController = navController, startDestination = "form1") {
        composable("form1") {
            val viewModel = viewModel<Form1ViewModel>(factory = form1ViewModelFactory(gBleService, "84:CC:A8:47:9B:56"))

            LaunchedEffect(viewModel.uiState.saveState) {
                if (viewModel.uiState.saveState) {
                    viewModel.resetSaveState()
                    navController.navigate("form2")
                }
            }

            DisposableEffect(true) {
                viewModel.init()
                onDispose {}
            }

            Form1Screen(
                viewModel.uiState,
                { i1, i2 -> viewModel.calcStartCurrent(i1, i2) },
                { navController.navigate("conf") },
                { startCurrent -> viewModel.further(startCurrent) },
            )
        }

        composable("conf") {
            val viewModel = viewModel<ConfViewModel>(factory = confViewModelFactory(gBleService, "84:CC:A8:47:9B:56"))

            LaunchedEffect(viewModel.uiState.saveState) {
                if (viewModel.uiState.saveState) {
                    viewModel.resetSaveState()
                    navController.popBackStack()
                }
            }

            DisposableEffect(true) {
                viewModel.init()
                onDispose {}
            }

            ConfScreen(
                viewModel.uiState,
                { kp, ki, kd -> viewModel.setConf(kp, ki, kd) },
            )
        }

        composable("form2") {
            val viewModel = viewModel<Form2ViewModel>()

            DisposableEffect(true) {
                viewModel.init()
                onDispose {
                    viewModel.deinit()
                }
            }

            Form2Screen(
                viewModel.uiState,
                { viewModel.start() },
                { viewModel.stop() },
                { viewModel.showResults() },
                { viewModel.hideResults() },
            )
        }
    }
}

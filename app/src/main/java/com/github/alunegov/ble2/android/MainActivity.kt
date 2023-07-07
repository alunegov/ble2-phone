package com.github.alunegov.ble2.android

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.alunegov.ble2.android.ui.theme.Ble2Theme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

//private const val TAG = "MainActivity"

/**
 * MAC-адрес выбранного сервера в окне Список серверов.
 */
// TODO: pass via nav args
var gAddress: String = "85:CC:A8:47:9B:56"  //"84:CC:A8:47:9B:56"

/**
 * Объект для работы с BT-адаптером.
 */
val gBleService = BleService()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        gAddress = sharedPref.getString("deviceId", gAddress) ?: gAddress

        setContent {
            Ble2Theme {
                /*val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()

                DisposableEffect(systemUiController, useDarkIcons) {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons,
                    )

                    onDispose {}
                }*/

                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RootWithLocationPermission(gBleService)
                }
            }
        }
    }

    override fun onPause() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        sharedPref.edit().putString("deviceId", gAddress).apply()

        super.onPause()
    }
}

/**
 * Основное окно приложения. Реализует работу с разрешениями.
 *
 * @param bleService Реализация [BleService].
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RootWithLocationPermission(bleService: BleService) {
    // не используем doNotShowRationale, п.ч. ble/location основа всей нашей функциональности

    val locationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
            )
        )
    } else {
        rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
    }

    if (locationPermissionState.allPermissionsGranted) {
        RootWithNavigation(bleService)
    } else {
        val textToShow = if (locationPermissionState.shouldShowRationale) {
            // If the user has denied the permission but the rationale can be shown,
            // then gently explain why the app requires this permission
            stringResource(R.string.permission_rationale)
        } else {
            // If it's the first time the user lands on this feature, or the user
            // doesn't want to be asked again for this permission, explain that the
            // permission is required
            stringResource(R.string.permission_denied)
        }
        PermissionNotGranted(
            textToShow,
            { locationPermissionState.launchMultiplePermissionRequest() },
        )
    }
}

@Composable
private fun PermissionNotGranted(
    text: String,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.widthIn(250.dp),
        ) {
            Text(stringResource(R.string.request_permission))
        }
    }
}

@Preview(locale = "ru", showBackground = true)
@Composable
fun PermissionNotGrantedPreview() {
    Ble2Theme {
        PermissionNotGranted(
            stringResource(R.string.permission_rationale),
            {},
        )
    }
}

@Composable
fun RootWithNavigation(bleService: BleService) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "form1") {
        composable("form1") {
            val viewModel = viewModel<Form1ViewModel>(factory = form1ViewModelFactory(bleService, gAddress))

            LaunchedEffect(viewModel.uiState.saveState) {
                if (viewModel.uiState.saveState) {
                    viewModel.resetSaveState()
                    navController.navigate("form2")
                }
            }

            DisposableEffect(true) {
                viewModel.ensureDevice(gAddress)
                onDispose {
                    viewModel.deinit()
                }
            }

            Form1Screen(
                viewModel.uiState,
                { navController.navigate("select_device") },
                { navController.navigate("conf") },
                { i1, i2 -> viewModel.calcStartCurrent(i1, i2) },
                { startCurrent -> viewModel.further(startCurrent) },
            )
        }

        composable("select_device") {
            val viewModel = viewModel<SelectDeviceViewModel>(factory = selectDeviceViewModelFactory(bleService))

            DisposableEffect(true) {
                viewModel.startScan()
                onDispose {
                    viewModel.stopScan()
                }
            }

            SelectDeviceScreen(
                viewModel.uiState.collectAsState().value,
                {
                    gAddress = it
                    navController.popBackStack()
                },
            )
        }

        composable("conf") {
            val viewModel = viewModel<ConfViewModel>(factory = confViewModelFactory(bleService, gAddress))

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
            val viewModel = viewModel<Form2ViewModel>(factory = form2ViewModelFactory(bleService, gAddress))

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

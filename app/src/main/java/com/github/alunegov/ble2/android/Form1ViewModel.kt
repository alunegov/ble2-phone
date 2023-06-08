package com.github.alunegov.ble2.android

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class Form1UiState(
    val connected: Boolean = false,
    val name: String? = null,
    val i1: Float = 0.0f,
    val i2: Float = 0.0f,
    val startCurrent: Float = 0.0f,
    val saveState: Boolean = false,
    val errorText: String = "",
)

class Form1ViewModel(
    private val bleService: BleService,
    private val deviceId: String,
): ViewModel() {
    var uiState by mutableStateOf(Form1UiState())
        private set

    private val connDispatchers = Dispatchers.IO
    private val connScope = CoroutineScope(viewModelScope.coroutineContext + connDispatchers)

    private val conn = bleService.getDeviceConn(deviceId, connScope)

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        Log.d(TAG, "onCleared")
        GlobalScope.launch(connDispatchers) {
            conn.disconnect()
        }
        super.onCleared()
    }

    fun init() {
        connScope.launch {
            try {
                conn.connect()

                uiState = uiState.copy(connected = true, name = conn.getName(), errorText = "")
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                uiState = uiState.copy(connected = false, name = null, errorText = e.message ?: e.toString())
            }
        }
    }

    fun calcStartCurrent(i1: Float, i2: Float) {
        uiState = uiState.copy(i1 = i1, i2 = i2)

        val startCurrent = (i2 * i1) / 100 * 2
        uiState = uiState.copy(i1 = i1, i2 = i2, startCurrent = startCurrent)
    }

    fun further(startCurrent: Float) {
        uiState = uiState.copy(startCurrent = startCurrent)

        connScope.launch {
            try {
                conn.connect()

                conn.setStartCurrent(startCurrent)
                uiState = uiState.copy(saveState = true, errorText = "")
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                uiState = uiState.copy(saveState = false, errorText = e.message ?: e.toString())
            }
        }
    }

    fun resetSaveState() {
        uiState = uiState.copy(saveState = false)
    }

    companion object {
        private const val TAG = "Form1ViewModel"
    }
}

fun form1ViewModelFactory(bleService: BleService, deviceId: String): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return Form1ViewModel(bleService, deviceId) as T
        }
    }
}

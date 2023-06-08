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

data class ConfUiState(
    val kp: Float = 0.0f,
    val ki: Float = 0.0f,
    val kd: Float = 0.0f,
    val saveState: Boolean = false,
    val errorText: String = "",
)

class ConfViewModel(
    private val bleService: BleService,
    private val deviceId: String,
): ViewModel() {
    var uiState by mutableStateOf(ConfUiState())
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

                uiState = uiState.copy(errorText = "")
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                uiState = uiState.copy(errorText = e.message ?: e.toString())
            }
        }
    }

    fun setConf(kp: Float, ki: Float, kd: Float) {
        uiState = uiState.copy(kp = kp, ki = ki, kd = kd)

        connScope.launch {
            try {
                conn.connect()

                conn.setConf(Conf(kp, ki, kd))
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
        private const val TAG = "ConfViewModel"
    }
}

fun confViewModelFactory(bleService: BleService, deviceId: String): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConfViewModel(bleService, deviceId) as T
        }
    }
}

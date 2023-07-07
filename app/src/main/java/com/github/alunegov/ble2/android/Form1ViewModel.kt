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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

data class Form1UiState(
    val connected: Boolean = false,
    val name: String? = null,
    val i1: Float = 0.0f,
    val i2: Float = 0.0f,
    val startCurrent: Float = 0.0f,
    val saveState: Boolean = false,
    val errorText: String = "",
    //val connStateText: String = "",
)

class Form1ViewModel(
    private val bleService: BleService,
    private var deviceId: String,
): ViewModel() {
    var uiState by mutableStateOf(Form1UiState())
        private set

    private val connDispatchers = Dispatchers.IO
    private val connScope = CoroutineScope(viewModelScope.coroutineContext + connDispatchers)

    private var conn = bleService.getDeviceConn(deviceId, connScope)

    private var autoReconnectJob: Job? = null

    private val reconnectDelay = AtomicInteger()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        Log.d(TAG, "onCleared")
        deinit()
        GlobalScope.launch(connDispatchers) {
            conn.disconnect()
        }
        super.onCleared()
    }

    fun ensureDevice(id: String) {
        if (deviceId != id) {
            deviceId = id

            connScope.launch {
                deinit()
                conn.disconnect()

                conn = bleService.getDeviceConn(deviceId, connScope)

                init()
            }
        } else {
            init()
        }
    }

    private fun enableAutoReconnect() {
        if (autoReconnectJob?.isActive != true) {
            autoReconnectJob = conn.connState
                /*.onEach {
                    Log.d(TAG, String.format("state=%s", it.toString()))
                    uiState = uiState.copy(connStateText = it.toString())
                }*/
                .filter { it is com.juul.kable.State.Disconnected }
                .onEach {
                    val delayMs = reconnectDelay.addAndGet(ReconnectDelayDelta).toLong()
                    Log.i(TAG, "Waiting $delayMs ms to reconnect...")
                    delay(delayMs);
                    connect();
                }
                .launchIn(connScope)
        }
    }

    private fun connect() {
        connScope.launch {
            try {
                conn.connect()

                val state = conn.getState()
                val startCurrent = conn.getStartCurrent()

                uiState = uiState.copy(
                    connected = true,
                    name = conn.getName(),
                    startCurrent = startCurrent,
                    saveState = (state == State.InTest) || (state == State.InMain),  // idle?
                    errorText = "",
                )

                reconnectDelay.set(0)
            } catch (e: Exception) {
                Log.w(TAG, e.toString())
                uiState = uiState.copy(connected = false, name = null, errorText = e.message ?: e.toString())
            }
        }
    }

    fun init() {
        Log.d(TAG, "init")

        enableAutoReconnect()

        connect()
    }

    fun deinit() {
        Log.d(TAG, "deinit")

        autoReconnectJob?.cancel()
        autoReconnectJob = null
    }

    fun calcStartCurrent(i1: Float, i2: Float) {
        uiState = uiState.copy(i1 = i1, i2 = i2)

        val startCurrent = (i2 * i1) / 100 * 2
        uiState = uiState.copy(startCurrent = startCurrent)
    }

    fun further(startCurrent: Float) {
        uiState = uiState.copy(startCurrent = startCurrent)

        connScope.launch {
            try {
                conn.connect()

                conn.setStartCurrent(startCurrent)
                uiState = uiState.copy(saveState = true, errorText = "")
            } catch (e: Exception) {
                Log.w(TAG, e.toString())
                uiState = uiState.copy(saveState = false, errorText = e.message ?: e.toString())
            }
        }
    }

    fun resetSaveState() {
        uiState = uiState.copy(saveState = false)
    }

    companion object {
        private const val TAG = "Form1ViewModel"

        private const val ReconnectDelayDelta = 2000;
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

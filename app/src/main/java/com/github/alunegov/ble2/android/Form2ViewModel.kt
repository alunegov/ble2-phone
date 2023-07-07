package com.github.alunegov.ble2.android

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.juul.kable.ConnectionLostException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.Exception
import kotlin.time.ExperimentalTime

data class Form2UiState(
    val connected: Boolean = false,
    val startCurrent: Float = 1.0f,
    val cycleNum: UInt = 1u,
    val currentUp: Float = 0.0f,
    val polarity: Boolean = false,
    val current: Float = 0.0f,
    val state: String = "",
    val startEnabled: Boolean = true,
    val stopEnabled: Boolean = false,
    val resultsAvail: Boolean = false,
    val results: List<CycleStat> = listOf(),
    val resultsDuration: Long = 0L,
    val showingResults: Boolean = false,
    val errorText: String = "",
    val connStateText: String = "",
)

class Form2ViewModel(
    private val bleService: BleService,
    private val deviceId: String,
) : ViewModel() {
    var uiState by mutableStateOf(Form2UiState())
        private set

    private val connDispatchers = Dispatchers.IO
    private val connScope = CoroutineScope(viewModelScope.coroutineContext + connDispatchers)

    private val conn = bleService.getDeviceConn(deviceId, connScope)

    private var autoReconnectJob: Job? = null
    private var stateCollectJob: Job? = null
    private var cycleCollectJob: Job? = null
    private var currentCollectJob: Job? = null

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

    private fun enableAutoReconnect() {
        if (autoReconnectJob?.isActive != true) {
            autoReconnectJob = conn.connState
                .onEach {
                    Log.d(TAG, "state=${it.toString()}")
                    uiState = uiState.copy(connStateText = it.toString())
                }
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

                val startCurrent = conn.getStartCurrent()
                val state = conn.getState()

                val idle = (state != State.InTest) && (state != State.InMain)
                if (idle) {
                    uiState = uiState.copy(
                        connected = true,
                        startCurrent = startCurrent,
                        state = state.toString(),
                        startEnabled = true,
                        stopEnabled = false,
                        errorText = "",
                    )
                } else {
                    val cycle = conn.getCycle()
                    val current = conn.getCurrent()

                    uiState = uiState.copy(
                        connected = true,
                        startCurrent = startCurrent,
                        cycleNum = cycle.num,
                        currentUp = cycle.currentUp,
                        polarity = cycle.polarity,
                        current = current,
                        state = state.toString(),
                        startEnabled = false,
                        stopEnabled = true,
                        errorText = "",
                    )
                }

                reconnectDelay.set(0)
            } catch (e: Exception) {
                Log.w(TAG, e.toString())
                uiState = uiState.copy(connected = false, startEnabled = false, stopEnabled = false, errorText = e.message ?: e.toString())
            }
        }
    }

    fun init() {
        Log.d(TAG, "init")

        enableAutoReconnect()

        connect()

        if (stateCollectJob?.isActive != true) {
            stateCollectJob = connScope.launch {
                conn.state.collect { state ->
                    val idle = (state != State.InTest) && (state != State.InMain)
                    if (idle) {
                        val ok = state == State.CyclesEnded
                        if (ok) {
                            val cyclesStat = conn.getCyclesStat()

                            uiState = uiState.copy(
                                state = state.toString(),
                                startEnabled = true,
                                stopEnabled = false,
                                resultsAvail = true,
                                results = cyclesStat,
                                resultsDuration = cyclesStat.sumOf { it.duration },
                                showingResults = false,
                                errorText = "",
                            )
                        } else {
                            uiState = uiState.copy(
                                state = state.toString(),
                                startEnabled = true,
                                stopEnabled = false,
                                errorText = "",
                            )
                        }
                    } else {
                        uiState = uiState.copy(
                            state = state.toString(),
                            errorText = "",
                        )
                    }
                }
            }
        }

        if (cycleCollectJob?.isActive != true) {
            cycleCollectJob = connScope.launch {
                conn.cycle.collect {
                    uiState = uiState.copy(
                        cycleNum = it.num,
                        currentUp = it.currentUp,
                        polarity = it.polarity,
                        errorText = "",
                    )
                }
            }
        }

        if (currentCollectJob?.isActive != true) {
            currentCollectJob = connScope.launch {
                conn.current.collect {
                    uiState = uiState.copy(current = it, errorText = "")
                }
            }
        }
    }

    fun deinit() {
        Log.d(TAG, "deinit")

        autoReconnectJob?.cancel()
        autoReconnectJob = null

        stateCollectJob?.cancel()
        stateCollectJob = null

        cycleCollectJob?.cancel()
        cycleCollectJob = null

        currentCollectJob?.cancel()
        currentCollectJob = null
    }

    fun start() {
        uiState = uiState.copy(
            state = "",
            startEnabled = false,
            stopEnabled = true,
            resultsAvail = false,
            errorText = "",
        )

        connScope.launch {
            try {
                conn.connect()

                conn.start()
            } catch (e: Exception) {
                Log.w(TAG, e.toString())
                uiState = uiState.copy(startEnabled = true, stopEnabled = false, errorText = e.message ?: e.toString())
            }
        }
    }

    fun stop() {
        uiState = uiState.copy(startEnabled = false, stopEnabled = false, errorText = "")

        connScope.launch {
            try {
                conn.connect()

                conn.stop()
                //uiState = uiState.copy(startEnabled = false, stopEnabled = false, errorText = "")
            } catch (e: Exception) {
                Log.w(TAG, e.toString())
                uiState = uiState.copy(startEnabled = false, stopEnabled = true, errorText = e.message ?: e.toString())
            }
        }
    }

    fun showResults() {
        uiState = uiState.copy(showingResults = true)
    }

    fun hideResults() {
        uiState = uiState.copy(showingResults = false)
    }

    companion object {
        private const val TAG = "Form2ViewModel"

        private const val ReconnectDelayDelta = 2000;
    }
}

fun form2ViewModelFactory(bleService: BleService, deviceId: String): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return Form2ViewModel(bleService, deviceId) as T
        }
    }
}

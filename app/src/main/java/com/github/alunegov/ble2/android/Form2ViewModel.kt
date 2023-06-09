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
import kotlinx.coroutines.launch
import kotlin.Exception
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

data class Form2UiState(
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
)

data class CycleStat(
    val cycle: Cycle,
    val current: Float,
    val duration: Long,
)

@OptIn(ExperimentalTime::class)
class Form2ViewModel(
    private val bleService: BleService,
    private val deviceId: String,
) : ViewModel() {
    var uiState by mutableStateOf(Form2UiState())
        private set

    private val connDispatchers = Dispatchers.IO
    private val connScope = CoroutineScope(viewModelScope.coroutineContext + connDispatchers)

    private val conn = bleService.getDeviceConn(deviceId, connScope)

    private var stateCollectJob: Job? = null
    private var cycleCollectJob: Job? = null
    private var currentCollectJob: Job? = null

    private val cycles = arrayListOf<CycleStat>()
    private var cycle = Cycle()
    private var cycleMaxCurrent = 0.0f
    private var cycleStartTime = TimeSource.Monotonic.markNow()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        Log.d(TAG, "onCleared")
        deinit()
        GlobalScope.launch(connDispatchers) {
            conn.disconnect()
        }
        super.onCleared()
    }

    fun init() {
        connScope.launch {
            try {
                conn.connect()

                val startCurrent = conn.getStartCurrent()
                val state = conn.getState()
                val idle = (state != State.InTest) && (state != State.InMain)
                if (idle) {
                    uiState = uiState.copy(
                        startCurrent = startCurrent,
                        state = "",//state.toString(),
                        startEnabled = true,
                        stopEnabled = false,
                        errorText = "",
                    )
                } else {
                    val cycle = conn.getCycle()
                    val current = conn.getCurrent()
                    uiState = uiState.copy(
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
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                uiState = uiState.copy(errorText = e.message ?: e.toString())
            }
        }

        if (stateCollectJob?.isActive != true) {
            stateCollectJob = connScope.launch {
                try {
                    conn.state.collect {state ->
                        val idle = (state != State.InTest) && (state != State.InMain)
                        if (idle) {
                            val ok = state == State.CyclesEnded
                            if (ok) {
                                uiState = uiState.copy(
                                    state = state.toString(),
                                    startEnabled = true,
                                    stopEnabled = false,
                                    resultsAvail = true,
                                    results = cycles,
                                    resultsDuration = cycles.sumOf { it.duration },
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
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                    uiState = uiState.copy(errorText = e.message ?: e.toString())
                }
            }
        }
        if (cycleCollectJob?.isActive != true) {
            cycleCollectJob = connScope.launch {
                try {
                    conn.cycle.collect {
                        if (it.num > 1u) {
                            val duration = (TimeSource.Monotonic.markNow() - cycleStartTime).inWholeSeconds
                            cycles.add(CycleStat(cycle, cycleMaxCurrent, duration))
                        }
                        cycle = it
                        cycleMaxCurrent = 0.0f
                        cycleStartTime = TimeSource.Monotonic.markNow()

                        uiState = uiState.copy(
                            cycleNum = it.num,
                            currentUp = it.currentUp,
                            polarity = it.polarity,
                            errorText = "",
                        )
                    }
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                    uiState = uiState.copy(errorText = e.message ?: e.toString())
                }
            }
        }
        if (currentCollectJob?.isActive != true) {
            currentCollectJob = connScope.launch {
                try {
                    conn.current.collect {
                        if (cycleMaxCurrent < it) {
                            cycleMaxCurrent = it
                        }

                        uiState = uiState.copy(current = it, errorText = "")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                    uiState = uiState.copy(errorText = e.message ?: e.toString())
                }
            }
        }
    }

    fun deinit() {
        Log.d(TAG, "deinit")
        stateCollectJob?.cancel()
        stateCollectJob = null
        cycleCollectJob?.cancel()
        cycleCollectJob = null
        currentCollectJob?.cancel()
        currentCollectJob = null
    }

    fun start() {
        cycles.clear()
        cycle = Cycle()
        cycleMaxCurrent = 0.0f
        cycleStartTime = TimeSource.Monotonic.markNow()

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
                Log.d(TAG, e.toString())
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
                Log.d(TAG, e.toString())
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
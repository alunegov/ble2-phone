package com.github.alunegov.ble2.android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class SelectDeviceViewModel(
    private val bleService: BleService,
) : ViewModel() {
    val uiState = bleService.devices

    private val connDispatchers = Dispatchers.IO
    private val connScope = CoroutineScope(viewModelScope.coroutineContext + connDispatchers)

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        //stopScan()
        super.onCleared()
    }

    /**
     * Запускает поиск серверов.
     */
    fun startScan() {
        bleService.startScan(connScope)
    }

    /**
     * Останавливает поиск серверов.
     */
    fun stopScan() {
        bleService.stopScan()
    }

    companion object {
        private const val TAG = "SelectDeviceViewModel"
    }
}

fun selectDeviceViewModelFactory(bleService: BleService): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectDeviceViewModel(bleService) as T
        }
    }
}

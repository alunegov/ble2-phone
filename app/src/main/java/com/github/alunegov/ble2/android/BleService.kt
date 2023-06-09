package com.github.alunegov.ble2.android

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import com.juul.kable.peripheral
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Device(
    val mac: String = "",
    val name: String? = null,
    val rssi: Int = 0,
)

data class DevicesModel(
    val devices: List<Device> = emptyList(),
    val errorText: String = "",
)

internal data class IntDevice(
    val adv: Advertisement,
    var lastSeen: Long,
)

class BleService {
    private val _devices = MutableStateFlow(DevicesModel())
    val devices = _devices.asStateFlow()

    private val intDevices = mutableMapOf<String, IntDevice>()

    private var scanJob: Job? = null

    fun startScan(scope: CoroutineScope) {
        if (scanJob?.isActive == true) {
            //Log.d(TAG, "already scanning")
            return
        }

        // resetting time to not exceed threshold on consecutive scans
        intDevices.values.forEach { it.lastSeen = System.currentTimeMillis() }

        scanJob = scope.launch {
            try {
                Scanner().advertisements.collect { adv ->
                    //Log.d(TAG, adv.toString())

                    if (adv.uuids.contains(uuidFrom(DeviceConn.SERVICE_UUID))) {
                        intDevices[adv.address] = IntDevice(adv, System.currentTimeMillis())
                    }

                    val timeThreshold = System.currentTimeMillis()
                    intDevices.entries.removeIf { (it.value.lastSeen + ServerTimeoutThreshold) <= timeThreshold }

                    _devices.value = DevicesModel(devices = intDevices.values.map { Device(it.adv.address, it.adv.name, it.adv.rssi) }, errorText = "")
                }
            } catch (ce: CancellationException) {
                //Log.d(TAG, ce.toString())
            } catch (e: Exception) {
                //Log.d(TAG, e.toString())
                _devices.value = DevicesModel(devices = emptyList(), errorText = e.message ?: e.toString())
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
    }

    fun getDeviceConn(id: String, scope: CoroutineScope): DeviceConn {
        return DeviceConn(scope.peripheral(id))
    }

    companion object {
        private const val TAG = "BleService"

        private const val ServerTimeoutThreshold = 5000L
    }
}

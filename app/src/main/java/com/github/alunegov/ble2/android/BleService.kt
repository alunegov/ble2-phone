package com.github.alunegov.ble2.android

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import com.juul.kable.peripheral
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class Device(
    val mac: String = "",
    val name: String? = null,
    val rssi: Int = 0,
)

internal data class IntDevice(
    val adv: Advertisement,
    var lastSeen: Long,
)

class BleService {
    var devices by mutableStateOf(listOf<Device>())
        private set

    private val intDevices = mutableMapOf<String, IntDevice>()

    private var scanJob: Job? = null

    fun startScan(scope: CoroutineScope) {
        if (scanJob?.isActive == true) {
            //logger?.d(TAG, "already scanning")
            return
        }

        // resetting time to not exceed threshold on consecutive scans
        intDevices.values.forEach { it.lastSeen = System.currentTimeMillis() }

        scanJob = scope.launch {
            try {
                Scanner().advertisements.collect { adv ->
                    if (adv.uuids.contains(uuidFrom(DeviceConn.SERVICE_UUID))) {
                        intDevices[adv.address] = IntDevice(adv, System.currentTimeMillis())
                    }

                    val timeThreshold = System.currentTimeMillis()
                    intDevices.entries.removeIf { (it.value.lastSeen + ServerTimeoutThreshold) <= timeThreshold }

                    devices = intDevices.values.map { Device(it.adv.address, it.adv.name, it.adv.rssi) }
                }
            } catch (ce: CancellationException) {
                //logger?.d(TAG, ce.toString())
            } catch (e: Exception) {
                //logger?.d(TAG, e.toString())
                devices = emptyList()  //ServersModel(errorText = e.message ?: e.toString())
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
        private const val ServerTimeoutThreshold = 5000L
    }
}

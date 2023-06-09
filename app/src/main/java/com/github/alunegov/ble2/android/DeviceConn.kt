package com.github.alunegov.ble2.android

import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Conf(
    val kp: Float = 0.0f,
    val ki: Float = 0.0f,
    val kd: Float = 0.0f,
)

data class Cycle(
    val num: UInt = 1u,
    val currentUp: Float = 0.0f,
    val polarity: Boolean = false,
)

enum class State {
    NoVoltageOffset,  // Не установлено смещение напряжения АЦП
    MakeSchema,       // Соберите цепь размагничивания
    CantSetCurrent,   // Не удалось установить требуемый ток, возможно большое сопротивление нагрузки
    CyclesEnded,      // Размагничивание завершено
    CyclesAborted,
    InTest,
    InMain;

    override fun toString() = when (ordinal) {
        0 -> "Не установлено смещение напряжения АЦП"
        1 -> "Соберите цепь размагничивания"
        2 -> "Не удалось установить требуемый ток, возможно большое сопротивление нагрузки"
        3 -> "Размагничивание завершено"
        4 -> "Размагничивание прервано"
        5 -> "Идёт тестирование"
        6 -> "Идёт размагничивание"
        else -> ""
    }

    companion object {
        fun fromByte(value: Byte) = values().first { it.ordinal == value.toInt() }
    }
}

class DeviceConn(
    private val periph: Peripheral,
) {
    //val connState = periph.state

    val state: Flow<State> = periph.observe(StateChr).map { it.state }

    val cycle: Flow<Cycle> = periph.observe(CycleChr).map { it.cycle }

    val current: Flow<Float> = periph.observe(CurrentChr).map { it.current }

    fun getName(): String? {
        return periph.name
    }

    suspend fun connect() {
        periph.connect()
    }

    suspend fun disconnect() {
        withTimeoutOrNull(5000) {
            periph.disconnect()
        }
    }

    suspend fun getConf(): Conf {
        val raw = periph.read(characteristicOf(SERVICE_UUID, CONF_CHR_UUID))
        if (raw.isEmpty()) {
            return Conf()
        }

        return ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).let {
            val kp = it.getFloat()
            val ki = it.getFloat()
            val kd = it.getFloat()
            Conf(kp, ki, kd)
        }
    }

    suspend fun setConf(conf: Conf) {
        val confSize = 12

        val raw = ByteBuffer.allocate(confSize).order(ByteOrder.LITTLE_ENDIAN).run {
            putFloat(conf.kp)
            putFloat(conf.ki)
            putFloat(conf.kd)
            array()
        }

        periph.write(characteristicOf(SERVICE_UUID, CONF_CHR_UUID), raw, WriteType.WithResponse)
    }

    suspend fun getStartCurrent(): Float {
        val raw = periph.read(characteristicOf(SERVICE_UUID, START_CURRENT_CHR_UUID))
        if (raw.isEmpty()) {
            return 0.0f
        }

        return raw.current
    }
    suspend fun setStartCurrent(startCurrent: Float) {
        val raw = ByteBuffer.allocate(Float.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN).putFloat(startCurrent).array()

        periph.write(characteristicOf(SERVICE_UUID, START_CURRENT_CHR_UUID), raw, WriteType.WithResponse)
    }

    suspend fun start() {
        periph.write(characteristicOf(SERVICE_UUID, CMD_CHR_UUID), byteArrayOf(1), WriteType.WithResponse)
    }

    suspend fun stop() {
        periph.write(characteristicOf(SERVICE_UUID, CMD_CHR_UUID), byteArrayOf(2), WriteType.WithResponse)
    }

    suspend fun getState(): State {
        val raw = periph.read(characteristicOf(SERVICE_UUID, STATE_CHR_UUID))
        assert(raw.isNotEmpty())

        return raw.state
    }

    suspend fun getCycle(): Cycle {
        val raw = periph.read(characteristicOf(SERVICE_UUID, CYCLE_CHR_UUID))
        if (raw.isEmpty()) {
            return Cycle()
        }

        return raw.cycle
    }

    suspend fun getCurrent(): Float {
        val raw = periph.read(characteristicOf(SERVICE_UUID, CURRENT_CHR_UUID))
        if (raw.isEmpty()) {
            return 0.0f
        }

        return raw.current
    }

    companion object {
        internal const val SERVICE_UUID = "ABAEBF19-EDE9-497D-A91D-0EF8B66A9904"
        internal const val CONF_CHR_UUID = "ABAEBF19-EDE9-497D-A91D-0EF8B66A9914"
        internal const val START_CURRENT_CHR_UUID = "ABAEBF19-EDE9-497D-A91D-0EF8B66A9924"
        internal const val CMD_CHR_UUID = "ABAEBF19-EDE9-497D-A91D-0EF8B66A9934"
        internal const val STATE_CHR_UUID = "ABAEBF19-EDE9-497D-A91D-0EF8B66A9944"
        internal const val CYCLE_CHR_UUID = "ABAEBF19-EDE9-497D-A91D-0EF8B66A9954"
        internal const val CURRENT_CHR_UUID = "ABAEBF19-EDE9-497D-A91D-0EF8B66A9964"

        private val StateChr = characteristicOf(
            service = SERVICE_UUID,
            characteristic = STATE_CHR_UUID,
        )

        private val CycleChr = characteristicOf(
            service = SERVICE_UUID,
            characteristic = CYCLE_CHR_UUID,
        )

        private val CurrentChr = characteristicOf(
            service = SERVICE_UUID,
            characteristic = CURRENT_CHR_UUID,
        )

        private inline val ByteArray.state: State
            get() = State.fromByte(ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).get())

        private inline val ByteArray.cycle: Cycle
            get() = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).let {
                val num = it.getInt().toUInt()
                val current = it.getFloat()
                val polarity = it.get() != 0.toByte()
                Cycle(num, current, polarity)
            }

        private inline val ByteArray.current: Float
            get() = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).getFloat()

    }
}

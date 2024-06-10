package ru.astar.osterrig.lamps

import android.bluetooth.BluetoothDevice
import android.content.Context
import ru.astar.osterrig.ReceiveDataCallback
import ru.astar.osterrig.connection.BleConnection
import ru.astar.osterrig.debug
import ru.astar.osterrig.entities.AbstractColor
import ru.astar.osterrig.entities.Command
import ru.astar.osterrig.entities.LampState
import ru.astar.osterrig.entities.RgbcwColor
import ru.astar.osterrig.toHexString
import java.nio.ByteBuffer

class Sirius(
    context: Context,
    device: BluetoothDevice,
) : BleConnection(context, device), Lamp, ReceiveDataCallback {

    private var _lampState = LampState()
    override val state: LampState
        get() = _lampState

    override suspend fun on() {
        debug("[$device] включение света")
    }

    override suspend fun off() {
        debug("[$device] выключение света")
    }

    override suspend fun setBrightness(brightness: Int) {
        _lampState = _lampState.copy(brightness = brightness)
        sendCommand(Command.Brightness(brightness))
    }

    override suspend fun setColor(color: AbstractColor) {
        _lampState = _lampState.copy(color = color)
        sendCommand(Command.SetColor(color))
    }

    override fun onReadyDevice() {
        debug("устройство готово к управлению\nзапрашиваем состояние устройства")
        addObserver(this)
        sendCommand(Command.GetState)
    }

    override fun onReleased() {
        debug("устройство в дисконнекте, освобождаем ресурсы")
        removeObserver(this)
    }

    override fun onLampStateValue(state: ByteArray) {
        parseLampState(state)
        debug("lamp state = ${state.toHexString()}")
    }

    override fun onBatteryValue(charge: ByteArray) {
        parseBatteryCharge(charge)
        debug("charge ${String(charge)}, ${charge.toHexString()}")
    }


    // todo вынести в отдельный класс
    private fun parseBatteryCharge(data: ByteArray) {
        val buffer = ByteBuffer.wrap(data)

    }

    // todo вынести в отдельный класс
    private fun parseLampState(data: ByteArray) {
        debug("lamp state ${data.toHexString()}")
        val buffer = ByteBuffer.wrap(data)
        buffer.position(1)

        val lightness = buffer.get().toInt() and 0xFF
        val color = RgbcwColor(
            red = buffer.get().toInt() and 0xFF,
            green = buffer.get().toInt() and 0xFF,
            blue = buffer.get().toInt() and 0xFF,
            cold = buffer.get().toInt() and 0xFF,
            warm = buffer.get().toInt() and 0xFF
        )

        _lampState = LampState(lightness, color)
        debug("lamp state = $state")
    }

}
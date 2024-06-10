package ru.astar.osterrig.lamps

import android.bluetooth.BluetoothDevice
import android.content.Context
import ru.astar.osterrig.ReceiveDataCallback
import ru.astar.osterrig.commands.Brightness
import ru.astar.osterrig.commands.GetState
import ru.astar.osterrig.commands.SetColor
import ru.astar.osterrig.connection.BleConnection
import ru.astar.osterrig.debug
import ru.astar.osterrig.entities.AbstractColor

class Sirius(
    context: Context,
    device: BluetoothDevice,
) : BleConnection(context, device), Lamp, ReceiveDataCallback {

    override suspend fun on() {
        debug("[$device] включение света")
    }

    override suspend fun off() {
        debug("[$device] выключение света")
    }

    override suspend fun setBrightness(brightness: Int) {
        sendCommand(Brightness(brightness))
    }

    override suspend fun setColor(color: AbstractColor) {
        sendCommand(SetColor(color))
    }

    override fun onReadyDevice() {
        debug("устройство готово к управлению\nзапрашиваем состояние устройства")
        addObserver(this)
        sendCommand(GetState())
    }

    override fun onReleased() {
        debug("устройство в дисконнекте, освобождаем ресурсы")
        removeObserver(this)
    }

    override fun onLampStateValue(state: ByteArray) {

    }

    override fun onBatteryValue(charge: ByteArray) {

    }
}
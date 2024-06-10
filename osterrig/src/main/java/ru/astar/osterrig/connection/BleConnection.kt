package ru.astar.osterrig.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import ru.astar.osterrig.commands.Command
import ru.astar.osterrig.Constants
import ru.astar.osterrig.ReceiveDataCallback
import ru.astar.osterrig.extensions.ConnectionException
import ru.astar.osterrig.extensions.NotSupportedException
import ru.astar.osterrig.debug
import ru.astar.osterrig.err
import java.util.LinkedList
import java.util.Queue
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

abstract class BleConnection(
    private val context: Context,
    protected val device: BluetoothDevice,
) : Connection {

    companion object {
        const val DEFAULT_MTU = 256
    }

    interface OnActionResult {
        fun onSuccess()
        fun onError(exception: Exception)
    }

    private var onActionResult: OnActionResult? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private var commandServiceUuid: UUID = UUID.fromString(Constants.COMMAND_SERVICE)
    private var batteryServiceUuid: UUID = UUID.fromString(Constants.BATTERY_SERVICE)
    private var sentCommandUuid: UUID = UUID.fromString(Constants.SENT_COMMAND)
    private var readValueUuid: UUID = UUID.fromString(Constants.READ_VALUE)
    private var batteryValueUuid: UUID = UUID.fromString(Constants.BATTERY_VALUE)

    private var sentCommandChr: BluetoothGattCharacteristic? = null
    private var readValueChr: BluetoothGattCharacteristic? = null
    private var batteryValueChr: BluetoothGattCharacteristic? = null

    private val commandsQueue: Queue<ByteArray> = LinkedList()
    private var pending: Boolean = false

    private var receiveDataCallback: ReceiveDataCallback? = null

    @SuppressLint("MissingPermission")
    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    bluetoothGatt = gatt
                    gatt?.requestMtu(DEFAULT_MTU)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    disconnect()
                }
            } else {
                onActionResult?.onError(ConnectionException(status))
                release()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                debug("[$device] mtu value ($mtu) changed successfully")
            } else {
                error("[$device] mtu value change error")
            }
            gatt?.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onActionResult?.onSuccess()
                debug("[$device] services founded")
            } else {
                onActionResult?.onError(ConnectionException(status))
                error("[$device] services not found")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int,
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onActionResult?.onSuccess()
            } else {
                onActionResult?.onError(Exception())
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                debug("[$device] data written!")
            }
            endCommand()
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            when (characteristic.uuid) {
                readValueUuid -> receiveDataCallback?.onLampStateValue(value)
                batteryValueUuid -> receiveDataCallback?.onBatteryValue(value)
            }
        }
    }

    override suspend fun connect() {
        if (bluetoothGatt != null) {
            debug("already connect to $device!")
            return
        }
        try {
            tryConnect()
            checkDeviceSupport()
            setupNotifications()
            onReadyDevice()
        } catch (e: Exception) {
            release()
            throw e
        }
    }

    override fun disconnect() {
        release()
    }

    abstract fun onReadyDevice()

    abstract fun onReleased()

    protected fun addObserver(callback: ReceiveDataCallback) {
        receiveDataCallback = callback
    }

    protected fun removeObserver(callback: ReceiveDataCallback) {
        receiveDataCallback = null
    }

    protected fun sendCommand(command: Command) {
        commandsQueue.add(command.array)
        debug("command queue size ${commandsQueue.size}")
        if (!pending) sendNextCommand()
    }

    private fun sendNextCommand() {
        Log.i("BleConnection", "sendNextCommand: before pending $pending")
        if (pending) {
            err("[$device] error! operation is pending!")
            return
        }

        val command = commandsQueue.poll() ?: run {
            err("[$device] command queue is empty!")
            return
        }

        pending = true

        writeData(command)

        Log.i("BleConnection", "sendNextCommand: after pending $pending")

    }

    private fun endCommand() {
        pending = false
        if (commandsQueue.isNotEmpty()) {
            sendNextCommand()
        }
    }

    @SuppressLint("MissingPermission")
    private fun writeData(data: ByteArray) {
        if (bluetoothGatt == null) {
            err("[$device] sent data error: no connection!")
            return
        }
        if (data.isEmpty()) {
            err("[$device] sent data error: no data for sending!")
            return
        }

        val characteristic = sentCommandChr ?: run {
            err("[$device] sent data error: characteristic not initialized!")
            return
        }

        characteristic.value = data
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        bluetoothGatt?.writeCharacteristic(characteristic)
    }

    fun setConfig(config: Config) {
        if (bluetoothGatt != null) {
            throw IllegalStateException("You cannot change the configuration while connected.")
        }
        config.commandServiceUuid?.let { commandServiceUuid = it }
        config.batteryServiceUuid?.let { batteryServiceUuid = it }
        config.commandSentUuid?.let { sentCommandUuid = it }
        config.readValueUuid?.let { readValueUuid = it }
        config.batteryValueUuid?.let { batteryValueUuid = it }

        debug("[$device] device configuration is set $config")
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryConnect() {
        debug("[$device] connecting...")
        device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        return suspendCoroutine {
            onActionResult = object : OnActionResult {
                override fun onSuccess() {
                    debug("[$device] connected!")
                    it.resume(Unit)
                }

                override fun onError(exception: Exception) {
                    it.resumeWithException(exception)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun release() {
        bluetoothGatt?.let {
            it.disconnect()
            it.close()
        }
        bluetoothGatt = null
        onActionResult = null
        sentCommandChr = null
        readValueChr = null
        batteryValueChr = null

        onReleased()
    }

    private fun checkDeviceSupport() {
        bluetoothGatt?.let {
            debug("[$device] check device support")

            val commandService = it.getService(commandServiceUuid)
            val batteryService = it.getService(batteryServiceUuid)

            if (commandService == null || batteryService == null) {
                err("device not supported: services not found")
                throw NotSupportedException()
            }

            sentCommandChr = commandService.getCharacteristic(sentCommandUuid)
            readValueChr = commandService.getCharacteristic(readValueUuid)
            batteryValueChr = batteryService.getCharacteristic(batteryValueUuid)

            if (sentCommandChr == null || readValueChr == null || batteryValueChr == null) {
                err("device not supported: characteristics not found")
                throw NotSupportedException()
            }
            debug("[$device] device supported!")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun setupNotifications() {
        debug("[$device] setup notifications...")
        val characteristics = arrayOf(readValueChr, batteryValueChr)
        for (characteristic in characteristics) {
            characteristic?.let { setupNotification(it) }
        }
        debug("[$device] all notifications enabled")
    }

    @SuppressLint("MissingPermission")
    private suspend fun setupNotification(characteristic: BluetoothGattCharacteristic): Boolean {
        val gatt = bluetoothGatt ?: return false
        val value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

        val descriptor = characteristic.getDescriptor(UUID.fromString(Constants.CCCD))
        descriptor.value = value
        gatt.setCharacteristicNotification(characteristic, true)
        gatt.writeDescriptor(descriptor)
        return suspendCoroutine {
            onActionResult = object : OnActionResult {
                override fun onSuccess() {
                    it.resume(true)
                }

                override fun onError(exception: Exception) {
                    it.resume(false)
                }
            }
        }
    }
}
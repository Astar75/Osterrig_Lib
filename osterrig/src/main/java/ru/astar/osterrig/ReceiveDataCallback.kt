package ru.astar.osterrig

interface ReceiveDataCallback {
    fun onLampStateValue(state: ByteArray)
    fun onBatteryValue(charge: ByteArray)
}
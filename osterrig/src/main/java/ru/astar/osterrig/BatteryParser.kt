package ru.astar.osterrig

import ru.astar.osterrig.exceptions.ParseDataException

class BatteryParser : Parser<ByteArray, BatteryData> {
    companion object {
        const val SEPARATOR = "#"
    }

    override fun parse(data: ByteArray): BatteryData {
        val string = String(data)
        if (string.indexOf(SEPARATOR) == -1) {
            throw ParseDataException()
        }
        val items = string.split(SEPARATOR)
        val charge = items[0].toInt()
        val timeUntilDischarge = items[1].toInt()
        val temperatureCelsius = items[2].toFloat()


        return BatteryData(charge = charge, temperature = Temperature(temperatureCelsius))
    }
}
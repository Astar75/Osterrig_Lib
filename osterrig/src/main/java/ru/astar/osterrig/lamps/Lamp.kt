package ru.astar.osterrig.lamps

import ru.astar.osterrig.connection.Connection
import ru.astar.osterrig.entities.AbstractColor
import ru.astar.osterrig.entities.LampState

interface Lamp : Connection {
    val state: LampState
    suspend fun on()
    suspend fun off()
    suspend fun setBrightness(brightness: Int)
    suspend fun setColor(color: AbstractColor)
}
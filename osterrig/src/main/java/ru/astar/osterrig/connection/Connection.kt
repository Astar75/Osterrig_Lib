package ru.astar.osterrig.connection

interface Connection {
    suspend fun connect()
    fun disconnect()
}
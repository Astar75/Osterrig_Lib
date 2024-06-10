package ru.astar.osterrig.commands

class Brightness(
    private val brightness: Int,
) : Command() {
    override val head: Byte = 0x1
    override val array: ByteArray get() = super.array + brightness.toByte()
}
package ru.astar.osterrig.commands

import ru.astar.osterrig.entities.AbstractColor

class SetColor(
    private val color: AbstractColor,
) : Command() {
    override val head: Byte = 0x16
    override val array: ByteArray get() = super.array + color.bytes
}
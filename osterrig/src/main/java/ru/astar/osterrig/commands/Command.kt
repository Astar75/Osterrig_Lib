package ru.astar.osterrig.commands

abstract class Command {
    abstract val head: Byte
    open val array: ByteArray get() = byteArrayOf(head)
}
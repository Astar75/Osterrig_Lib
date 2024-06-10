package ru.astar.osterrig.entities


sealed class Command {

    abstract val head: Byte
    open val array : ByteArray get() = byteArrayOf(head)
    data object GetState: Command() {
        override val head: Byte get() = 0x40
    }

    data class Brightness(
        private val brightness: Int,
    ) : Command() {
        override val head: Byte = 0x1
        override val array: ByteArray get() = super.array + brightness.toByte()
    }

    data class SetColor(
        private val color: AbstractColor,
    ) : Command() {
        override val head: Byte = 0x16
        override val array: ByteArray get() = super.array + color.bytes
    }
}

package ru.astar.osterrig.entities

abstract class AbstractColor {
    val red: Int
    val green: Int
    val blue: Int

    companion object {
        fun pack(red: Int, green: Int, blue: Int): Int {
            return (red shl 16) or (green shl 8) or blue
        }
    }

    constructor(red: Int, green: Int, blue: Int) {
        require(red in 0x0..0xFF) { "Red value ($red) must be in range 0..255" }
        require(green in 0x0..0xFF) { "Green value ($green) must be in range 0..255" }
        require(blue in 0x0..0xFF) { "Blue value ($blue) must be in range 0..255" }

        this.red = red
        this.green = green
        this.blue = blue
    }

    constructor(rgb: Int) {
        red = (rgb shr 16) and 0xFF
        green = (rgb shr 8) and 0xFF
        blue = rgb and 0xFF
    }

    open val rgb: Int
        get() = pack(red, green, blue)

    open val bytes: ByteArray
        get() = byteArrayOf(red.toByte(), green.toByte(), blue.toByte())

    override fun toString(): String {
        return "Color[red = $red, green = $green, blue = $blue]"
    }
}

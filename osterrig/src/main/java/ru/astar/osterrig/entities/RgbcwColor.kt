package ru.astar.osterrig.entities

class RgbcwColor : AbstractColor {
    val cold: Int
    val warm: Int

    constructor(red: Int, green: Int, blue: Int, cold: Int = 0, warm: Int = 0) : super(
        red,
        green,
        blue
    ) {
        require(red in 0x0..0xFF) { "Cold value ($cold) must be in range 0..255" }
        require(green in 0x0..0xFF) { "Warm value ($warm) must be in range 0..255" }

        this.cold = cold
        this.warm = warm
    }

    constructor(color: Int) : super(color) {
        this.cold = 0
        this.warm = 0
    }

    override val rgb: Int
        get() {
            val sum = cold + warm
            val r = (red + sum).coerceAtMost(0xFF)
            val g = (green + sum).coerceAtMost(0xFF)
            val b = (blue + sum).coerceAtMost(0xFF)
            return pack(r, g, b)
        }

    override val bytes: ByteArray
        get() = super.bytes + byteArrayOf(cold.toByte(), warm.toByte())

    override fun toString(): String {
        return "Color[red=$red, green=$green, blue=$blue, cold=$cold, warm=$warm]"
    }
}
package ru.astar.osterrig

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryParserTest {

    @Test
    fun parse_battery_charge_from_string() {
        val source = byteArrayOf(
            0x35, 0x35, 0x23, 0x31, 0x39,
            0x33, 0x36, 0x23, 0x33, 0x31,
            0x2E, 0x35, 0x36, 0x34, 0x37,
            0x34, 0x31
        )

        println(String(source))

        val parser = BatteryParser()
        val expectedBatteryCharge = BatteryData(55)
        val actualBatteryCharge = parser.parse(source)

        assertEquals(expectedBatteryCharge, actualBatteryCharge)
    }
}
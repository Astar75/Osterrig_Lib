package ru.astar.osterrig

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUntilDischargeTest {

    @Test
    fun test_convert_time() {
        val minutes = 5986
        val expectedHours = 99
        val expectedMinutes = 46

        val clazz = TimeUntilDischarge(minutes)
        assertEquals(expectedHours, clazz.hours)
        assertEquals(expectedMinutes, clazz.minutes)
    }

    @Test
    fun test_format_time() {
        var minutes = 120
        var clazz = TimeUntilDischarge(minutes)
        var actual = clazz.get()
        assertEquals("2H0", actual)
        actual = clazz.get(false)
        assertEquals("02:00", actual)

        minutes = 30
        clazz = TimeUntilDischarge(minutes)
        actual = clazz.get()
        assertEquals("M30", actual)
        actual = clazz.get(false)
        assertEquals("00:30", actual)

        minutes = 64
        clazz = TimeUntilDischarge(minutes)
        actual = clazz.get()
        assertEquals("1H4", actual)
        actual = clazz.get(false)
        assertEquals("01:04", actual)
    }
}
package ru.astar.osterrig

import android.annotation.SuppressLint

data class BatteryData(
    val charge: Int,
    val timeDischarge: TimeUntilDischarge? = null,
    val temperature: Temperature? = null,
)

data class TimeUntilDischarge(
    val hours: Int,
    val minutes: Int,
) {
    // Конструктор, принимающий количество минут
    constructor(minutes: Int) : this(
        hours = (minutes * 60) / 3600,
        minutes = ((minutes * 60) % 3600) / 60,
    )

    @SuppressLint("DefaultLocale")
    fun get(specialFormat: Boolean = true): String {
        return if (specialFormat) {
            if (hours > 0) String.format("%dH%d", hours, minutes)
            else String.format("M%d", minutes)
        } else String.format("%02d:%02d", hours, minutes)
    }
}

data class Temperature(
    val celsius: Float,
)
package ru.astar.osterrig.entities

data class LampState(
    val brightness: Int = 0,
    val color: AbstractColor? = null,
)
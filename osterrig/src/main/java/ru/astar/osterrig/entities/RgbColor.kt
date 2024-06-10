package ru.astar.osterrig.entities

class RgbColor : AbstractColor {
    constructor(red: Int, green: Int, blue: Int) : super(red, green, blue)
    constructor(color: Int) : super(color)
}
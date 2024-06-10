package ru.astar.osterrig

interface Parser<I, R> {
    fun parse(data: I): R
}
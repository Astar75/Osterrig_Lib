package ru.astar.osterrig

fun ByteArray.toHexString() = joinToString(separator = " ") { String.format("%02X", it) }
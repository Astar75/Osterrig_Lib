package ru.astar.osterrig

import android.util.Log

fun Any.debug(message: String, tag: String? = null) {
    Log.d(tag ?: this::class.java.simpleName, message)
}

fun Any.err(message: String, exception: Exception? = null, tag: String? = null) {
    Log.e(tag ?: this::class.java.simpleName, message, exception)
}
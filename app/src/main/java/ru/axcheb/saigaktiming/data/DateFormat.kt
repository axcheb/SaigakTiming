package ru.axcheb.saigaktiming.data

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

private val ddmmyyyyhhmmFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
private val hhmmsssssFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
private val hhmmFormat = SimpleDateFormat("HH:mm", Locale.US)
private val hhmmssFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
private val ddmmyyyyFormat = SimpleDateFormat("dd.MM.yyyy", Locale.US)

fun Date.ddmmyyyyhhmmStr(): String {
    return ddmmyyyyhhmmFormat.format(this)
}

fun Date.hhmmsssssStr(): String {
    return hhmmsssssFormat.format(this)
}

fun Date.hhmmStr(): String {
    return hhmmFormat.format(this)
}

fun Date.hhmmssStr(): String {
    return hhmmssFormat.format(this)
}

fun Date.ddmmyyyyStr(): String {
    return ddmmyyyyFormat.format(this)
}

fun Long.formatElapsedTimeSeconds(): String {
    return DateUtils.formatElapsedTime(this)
}

fun Long.formatElapsedTimeMs(): String {
    return "${DateUtils.formatElapsedTime(this / 1000)}.${this % 1000}"
}

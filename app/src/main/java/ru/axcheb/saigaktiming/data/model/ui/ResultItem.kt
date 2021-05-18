package ru.axcheb.saigaktiming.data.model.ui

import java.text.SimpleDateFormat
import java.util.*

data class ResultItem(
    /** startId или finishId */ // TODO порефакторить
    val id: Long,
    val sensor: Long,
    val startTime: Date,
    val finishTime: Date,
    val isActive: Boolean
) {

    val sensorStr get() = sensor.toString()
    val startTimeStr get() = dateFormat.format(startTime)
    val finishTimeStr get() = dateFormat.format(finishTime)
    val resultStr: String get() {
        return "${diff / 1000}.${diff % 1000}"
    }

    val diff: Long get() = finishTime.time - startTime.time

    companion object {
        private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    }

}
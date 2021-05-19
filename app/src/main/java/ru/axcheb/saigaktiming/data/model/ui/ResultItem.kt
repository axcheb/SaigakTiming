package ru.axcheb.saigaktiming.data.model.ui

import ru.axcheb.saigaktiming.data.hhmmssStr
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
    val startTimeStr get() = startTime.hhmmssStr()
    val finishTimeStr get() = finishTime.hhmmssStr()
    val resultStr: String get() {
        return "${diff / 1000}.${diff % 1000}"
    }

    val diff: Long get() = finishTime.time - startTime.time

}
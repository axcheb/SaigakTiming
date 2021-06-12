package ru.axcheb.saigaktiming.data.model.ui

import ru.axcheb.saigaktiming.data.formatSecondsAndMs
import ru.axcheb.saigaktiming.data.hhmmssStr
import java.util.*

abstract class ResultItem {

    abstract val id: Long
    abstract val sensor: Int
    abstract val startTime: Date
    abstract val finishTime: Date
    abstract val isActive: Boolean

    val sensorStr get() = sensor.toString()
    val startTimeStr get() = startTime.hhmmssStr()
    val finishTimeStr get() = finishTime.hhmmssStr()
    val resultStr: String get() = diff.formatSecondsAndMs()
    val diff: Long get() = finishTime.time - startTime.time

}
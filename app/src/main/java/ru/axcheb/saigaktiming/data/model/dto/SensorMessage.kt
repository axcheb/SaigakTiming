package ru.axcheb.saigaktiming.data.model.dto

import ru.axcheb.saigaktiming.data.FINISH
import java.util.*

data class SensorMessage(
    val command: String,
    val sensorId: Int,
    val date: Date
) {
    fun isFinish(): Boolean = command == FINISH
}
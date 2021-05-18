package ru.axcheb.saigaktiming.data.model.ui

/** Результаты на СУ для финишного протокола. */
data class ProtocolFinishItem (
    val startId: Long,
    val finishId: Long,
    val sensorId: Int,
    val resultMillis: Long
)
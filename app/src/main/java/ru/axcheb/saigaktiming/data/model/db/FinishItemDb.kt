package ru.axcheb.saigaktiming.data.model.db

import java.util.*

class FinishItemDb(
    val id: Long,
    val sensor: Int,
    val startTime: Date,
    val finishTime: Date,
    val isActive: Boolean
)
package ru.axcheb.saigaktiming.data.model.ui

import java.util.*

data class StartItem(
    override val id: Long,
    override val sensor: Int,
    override val startTime: Date,
    override val finishTime: Date,
    override val isActive: Boolean
): ResultItem()

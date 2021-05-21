package ru.axcheb.saigaktiming.data.model.ui

import ru.axcheb.saigaktiming.data.hhmmStr
import java.util.*

data class EventMemberItem (
    val id: Long,
    val eventId: Long,
    val memberId: Long,
    val sequenceNumber: Int,
    val name: String,
    val isNext: Boolean,
    val startTimes: List<Date> = emptyList()
) {

    fun startTimesStr(): String = startTimes.joinToString {
        it.hhmmStr()
    }

}
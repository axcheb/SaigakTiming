package ru.axcheb.saigaktiming.data.model.ui

import ru.axcheb.saigaktiming.data.ddmmyyyyhhmmStr
import java.util.*

data class ArchiveItem (
    var eventId: Long,
    var date: Date,
) {
    fun getDateStr(): String = date.ddmmyyyyhhmmStr()
}

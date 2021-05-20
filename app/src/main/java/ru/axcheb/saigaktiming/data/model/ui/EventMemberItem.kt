package ru.axcheb.saigaktiming.data.model.ui

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ru.axcheb.saigaktiming.data.hhmmStr
import ru.axcheb.saigaktiming.data.model.dto.Event
import java.util.*

data class EventMemberItem @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "member_id") val memberId: Long,
    @ColumnInfo(name = "sequence_number") var sequenceNumber: Int,
    var name: String,
    @Ignore private var startTimes: List<Date> = emptyList()
) {

    /** Рассчитывает времена старта участника. */
    fun calculateStartDates(event: Event, membersCount: Int) {
        val minutesBetweenTrack = membersCount * event.trackMaxTime
        val calendar = GregorianCalendar.getInstance()
        calendar.time = event.date
        // Время первого старта этого участника
        calendar.add(Calendar.MINUTE, (sequenceNumber - 1) * event.trackMaxTime)
        startTimes = List(event.trackCount) {
            val date = calendar.time
            calendar.add(Calendar.MINUTE, minutesBetweenTrack)
            date
        }
    }

    fun startTimesStr(): String = startTimes.joinToString {
        it.hhmmStr()
    }

}
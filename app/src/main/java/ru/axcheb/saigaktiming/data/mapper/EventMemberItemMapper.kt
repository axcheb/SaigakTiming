package ru.axcheb.saigaktiming.data.mapper

import ru.axcheb.saigaktiming.data.model.db.Event
import ru.axcheb.saigaktiming.data.model.db.EventMemberCrossRef
import ru.axcheb.saigaktiming.data.model.db.EventMemberCrossRefAndMember
import ru.axcheb.saigaktiming.data.model.ui.EventMemberItem
import java.util.*

class EventMemberItemMapper :
    Mapper<Triple<EventMemberCrossRefAndMember, Event, Int>, EventMemberItem> {
    override fun map(input: Triple<EventMemberCrossRefAndMember, Event, Int>): EventMemberItem {
        val eventMember = input.first.eventMemberCrossRef
        val member = input.first.member
        val event = input.second
        return EventMemberItem(
            id = eventMember.id!!,
            eventId = eventMember.eventId!!,
            memberId = member.id!!,
            sequenceNumber = eventMember.sequenceNumber,
            name = member.name,
            isNext = eventMember.sequenceNumber == event.currentMemberIndex + 1,
            startTimes = calculateStartDates(
                eventMember,
                event,
                input.third
            )
        )
    }

    /** Рассчитывает времена старта участника. */
    private fun calculateStartDates(
        eventMemberCrossRef: EventMemberCrossRef,
        event: Event,
        membersCount: Int
    ): List<Date> {
        val minutesBetweenTrack = membersCount * event.trackMaxTime
        val calendar = GregorianCalendar.getInstance()
        calendar.time = event.date
        // Время первого старта этого участника
        calendar.add(Calendar.MINUTE, (eventMemberCrossRef.sequenceNumber - 1) * event.trackMaxTime)
        return List(event.trackCount) {
            val date = calendar.time
            calendar.add(Calendar.MINUTE, minutesBetweenTrack)
            date
        }
    }

}
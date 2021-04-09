package ru.axcheb.saigaktiming.data.repository

import ru.axcheb.saigaktiming.data.dao.EventDao
import ru.axcheb.saigaktiming.data.model.domain.Event
import java.util.*

class EventRepository(private val eventDao: EventDao) {

    fun getCurrentEvent() = eventDao.getCurrentEvent()

    suspend fun insert(event: Event) = eventDao.insert(event)

    suspend fun update(event: Event) = eventDao.update(event)

    suspend fun newEvent(): Event {
        val event = Event(null, Date())
        val eventId = eventDao.insert(event)
        event.id = eventId
        return event
    }

}
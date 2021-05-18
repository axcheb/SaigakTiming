package ru.axcheb.saigaktiming.data.repository

import ru.axcheb.saigaktiming.data.dao.EventDao
import ru.axcheb.saigaktiming.data.model.dto.Event

class EventRepository(private val eventDao: EventDao) {

    fun getCurrentEvent() = eventDao.getCurrentEvent()

    fun getEvent(id: Long) = eventDao.getEvent(id)

    suspend fun insert(event: Event) = eventDao.insert(event)

    suspend fun update(event: Event) = eventDao.update(event)

}
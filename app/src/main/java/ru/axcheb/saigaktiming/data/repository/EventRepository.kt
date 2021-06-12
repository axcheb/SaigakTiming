package ru.axcheb.saigaktiming.data.repository

import kotlinx.coroutines.flow.map
import ru.axcheb.saigaktiming.data.dao.EventDao
import ru.axcheb.saigaktiming.data.mapper.ArchiveItemMapper
import ru.axcheb.saigaktiming.data.mapper.ListMapper
import ru.axcheb.saigaktiming.data.model.db.Event

class EventRepository(private val eventDao: EventDao) {

    private val listOfArchiveItemMapper = ListMapper(ArchiveItemMapper())

    fun getCurrentEvent() = eventDao.getCurrentEvent()

    fun getEvent(id: Long) = eventDao.getEvent(id)

    fun getArchivedItems() = eventDao.getArchivedEvents().map { listOfArchiveItemMapper.map(it) }

    suspend fun insert(event: Event) = eventDao.insert(event)

    suspend fun update(event: Event) = eventDao.update(event)

}
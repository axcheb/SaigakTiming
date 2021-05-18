package ru.axcheb.saigaktiming.data.repository

import ru.axcheb.saigaktiming.data.dao.ResultDao
import ru.axcheb.saigaktiming.data.mapper.ListOfProtocolItemMapper
import ru.axcheb.saigaktiming.data.model.dto.Finish
import ru.axcheb.saigaktiming.data.model.dto.Start
import ru.axcheb.saigaktiming.data.model.ui.ProtocolItem

class ResultRepository(private val resultDao: ResultDao) {

    private val listOfProtocolItemMapper = ListOfProtocolItemMapper()

    fun getStart(id: Long) = resultDao.getStart(id)

    fun getFinishResults(startId: Long) = resultDao.getFinishResults(startId)

    suspend fun insert(finish: Finish) = resultDao.insert(finish)

    suspend fun insert(start: Start) = resultDao.insert(start)

    fun getStartResults(eventId: Long, memberId: Long) =
        resultDao.getStartResults(eventId, memberId)

    suspend fun inactivateFinish(startId: Long) = resultDao.inactivateFinish(startId)

    suspend fun insertFinishAsOnlyActiveOne(finish: Finish) =
        resultDao.insertFinishAsOnlyActiveOne(finish)

    suspend fun makeFinishAsOnlyActiveOne(finishId: Long) =
        resultDao.makeFinishAsOnlyActiveOne(finishId)

    suspend fun inactivateStart(startId: Long) = resultDao.inactivateStart(startId)

    suspend fun activateStart(startId: Long) = resultDao.activateStart(startId)

    suspend fun getProtocolItems(eventId: Long): List<ProtocolItem> {
        return listOfProtocolItemMapper.map(resultDao.getProtocolData(eventId))
    }

}
package ru.axcheb.saigaktiming.data.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.firstOrNull
import ru.axcheb.saigaktiming.data.dao.ResultDao
import ru.axcheb.saigaktiming.data.model.domain.Finish
import ru.axcheb.saigaktiming.data.model.domain.Start

class ResultRepository(private val resultDao: ResultDao) {

    fun getStart(id: Long) = resultDao.getStart(id)

    fun getFinishResults(startId: Long) = resultDao.getFinishResults(startId)

    suspend fun insert(finish: Finish) = resultDao.insert(finish)

    suspend fun insert(start: Start) = resultDao.insert(start)

    fun getStartResults(eventId: Long, memberId: Long) = resultDao.getStartResults(eventId, memberId)

    suspend fun inactivateFinish(startId: Long) = resultDao.inactivateFinish(startId)

    // TODO запускать в одной транзакции: либо перенести в DAO и повесить аннотацию, либо runInTransaction
    // TODO проверить где ещё вызывается похожий код
    suspend fun insertFinishAsOnlyActiveOne(finish: Finish): Long {
        resultDao.inactivateFinish(finish.startId)
        return resultDao.insert(finish)
    }

    // TODO транзакция
    suspend fun makeFinishAsOnlyActiveOne(finishId: Long) {
        val finish = resultDao.getFinish(finishId).firstOrNull() ?: return
        resultDao.inactivateFinish(finish.startId)
        resultDao.activateFinish(finishId)
    }

    suspend fun inactivateStart(startId: Long) = resultDao.inactivateStart(startId)

    suspend fun activateStart(startId: Long) = resultDao.activateStart(startId)

}
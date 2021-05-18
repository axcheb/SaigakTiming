package ru.axcheb.saigaktiming.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import ru.axcheb.saigaktiming.data.model.dto.EventMemberCrossRefAndMember
import ru.axcheb.saigaktiming.data.model.dto.Finish
import ru.axcheb.saigaktiming.data.model.dto.Start
import ru.axcheb.saigaktiming.data.model.dto.StartAndFinish
import ru.axcheb.saigaktiming.data.model.ui.ResultItem

@Dao
interface ResultDao {

    @Query("select * from start where id = :id")
    fun getStart(id: Long): Flow<Start>

    @Query("select * from finish where id = :id")
    fun getFinish(id: Long): Flow<Finish>

    @Insert
    suspend fun insert(finish: Finish): Long

    @Insert
    suspend fun insert(finish: Start): Long

    @Query(
        """
        select 
            start.id as id,
            finish.sensor_id as sensor
            ,start.time as startTime
            ,finish.time as finishTime
            ,start.is_active as isActive 
        from event_member
        join start on start.event_member_id = event_member.id
        join finish on finish.start_id = start.id
        where 
            event_id = :eventId 
            and member_id = :memberId
            and finish.is_active = 1
        order by start.time desc
    """
    )
    fun getStartResults(eventId: Long, memberId: Long): Flow<List<ResultItem>>

    @Query(
        """
        select 
            finish.id as id
            ,finish.sensor_id as sensor
            ,start.time as startTime
            ,finish.time as finishTime
            ,finish.is_active as isActive
        from finish
            join start on finish.start_id = start.id
        where finish.start_id = :startId 
        order by finishTime desc
    """
    )
    fun getFinishResults(startId: Long): Flow<List<ResultItem>>

    /**
     * Ставит isActive = 0 всем финишным точкам старта startId.
     */
    @Query("update finish set is_active = 0 where start_id = :startId")
    suspend fun inactivateFinish(startId: Long)

    @Query("update finish set is_active = 1 where id = :finishId")
    suspend fun activateFinish(finishId: Long)

    @Query("update start set is_active = 0 where id = :startId")
    suspend fun inactivateStart(startId: Long)

    @Query("update start set is_active = 1 where id = :startId")
    suspend fun activateStart(startId: Long)

    @Transaction
    suspend fun makeFinishAsOnlyActiveOne(finishId: Long) {
        val finish = getFinish(finishId).flowOn(Dispatchers.Default).firstOrNull() ?: return
        inactivateFinish(finish.startId)
        activateFinish(finishId)
    }

    @Transaction
    suspend fun insertFinishAsOnlyActiveOne(finish: Finish): Long {
        inactivateFinish(finish.startId)
        return insert(finish)
    }

    @Transaction
    @Query("select * from event_member where event_id = :eventId")
    suspend fun getEventMemberCrossRefAndMember(eventId: Long): List<EventMemberCrossRefAndMember>

    @Query(
        """
            select 
                start.id as start_id,
                start.event_member_id as start_event_member_id,
                start.time as start_time,
                start.is_active as start_is_active,
                finish.id as finish_id,
                finish.start_id as finish_start_id,
                finish.time as finish_time,
                finish.sensor_id as finish_sensor_id,
                finish.is_active as finish_is_active  
            from start
                join finish on finish.start_id = start.id
            where start.is_active = 1 
                and finish.is_active = 1
                and start.event_member_id in (:eventMemberIds)
        """
    )
    suspend fun getActiveStartAndFinishForEventMembers(eventMemberIds: List<Long>): List<StartAndFinish>

    @Transaction
    suspend fun getProtocolData(eventId: Long): Pair<List<EventMemberCrossRefAndMember>, List<StartAndFinish>> {
        val eventMembers = getEventMemberCrossRefAndMember(eventId)
        val eventMemberIds = eventMembers.map { it.eventMemberCrossRef.id!! }
        val startAndFinishList = getActiveStartAndFinishForEventMembers(eventMemberIds)
        return Pair(eventMembers, startAndFinishList)
    }


}
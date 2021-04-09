package ru.axcheb.saigaktiming.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.axcheb.saigaktiming.data.model.domain.Finish
import ru.axcheb.saigaktiming.data.model.domain.Start
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

    @Query("""
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
    """)
    fun getStartResults(eventId: Long, memberId: Long): Flow<List<ResultItem>>

    @Query("""
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
    """)
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

}
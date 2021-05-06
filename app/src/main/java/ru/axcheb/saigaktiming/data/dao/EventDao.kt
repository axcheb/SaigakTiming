package ru.axcheb.saigaktiming.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.axcheb.saigaktiming.data.model.domain.Event
import ru.axcheb.saigaktiming.data.model.domain.Member

@Dao
interface EventDao {

    @Query(value = "select * from event where in_archive = 0")
    fun getCurrentEvent(): Flow<Event?>

    @Query(value = "select * from event where id = :id")
    fun getEvent(id: Long): Flow<Event>

    @Insert
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(vararg event: Event)

}